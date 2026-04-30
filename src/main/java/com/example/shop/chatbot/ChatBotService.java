package com.example.shop.chatbot;

import com.example.shop.entity.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatBotService {

    private static final String FALLBACK_SCOPE_MESSAGE = "Mình là trợ lý mua sắm, mình có thể hỗ trợ bạn tìm sản phẩm, giá, tồn kho và đơn hàng trong shop.";
    private static final String SHOP_ADDRESS = "97 Man Thiện, Phường Tăng Nhơn Phú, TP. Hồ Chí Minh.";
    private static final DateTimeFormatter ORDER_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d+[\\.,]?\\d*)\\s*(k|nghìn|nghin|tr|triệu|trieu)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern ORDER_PATTERN = Pattern.compile("\\b([A-Za-z]{2,5}[-_]?[0-9]{2,10})\\b");

    private final ChatShopQueryRepository chatShopQueryRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String openAiModel;

    public ChatBotService(ChatShopQueryRepository chatShopQueryRepository) {
        this.chatShopQueryRepository = chatShopQueryRepository;
    }

    public ChatResponse ask(ChatRequest request) {
        String message = request != null && request.getMessage() != null ? request.getMessage().trim() : "";
        if (message.isBlank()) {
            return new ChatResponse("Bạn vui lòng nhập câu hỏi trước khi gửi.");
        }

        String normalized = normalize(message);
        String ruleAnswer = answerByRules(message, normalized);

        if (isOpenAiConfigured()) {
            try {
                String aiAnswer = askOpenAi(message, ruleAnswer);
                if (!aiAnswer.isBlank()) {
                    return new ChatResponse(aiAnswer);
                }
            } catch (Exception e) {
                System.err.println("=== CHATBOT GEMINI ERROR: " + e.getMessage());
                // Nếu OpenAI lỗi thì fallback về rule-based
            }
        }
        return new ChatResponse(ruleAnswer);
    }

    private String answerByRules(String originalMessage, String normalized) {
        if (isGreeting(normalized)) {
            return FALLBACK_SCOPE_MESSAGE;
        }

        if (isAddressQuestion(normalized)) {
            return "Địa chỉ shop: " + SHOP_ADDRESS;
        }

        if (isOutOfScope(normalized)) {
            return FALLBACK_SCOPE_MESSAGE;
        }

        if (normalized.contains("bao nhieu san pham") || normalized.contains("tong so san pham")) {
            return "Hiện tại shop đang có khoảng " + chatShopQueryRepository.countProducts() + " sản phẩm.";
        }

        Optional<String> orderCode = extractOrderCode(originalMessage);
        if (isOrderQuestion(normalized) && orderCode.isPresent()) {
            Optional<Order> order = chatShopQueryRepository.getOrderStatus(orderCode.get());
            if (order.isEmpty()) {
                return "Mình chưa tìm thấy đơn hàng " + orderCode.get().toUpperCase(Locale.ROOT) + ". Bạn kiểm tra lại mã đơn giúp mình nhé.";
            }
            Order found = order.get();
            String orderTime = found.getNgayDat() != null ? found.getNgayDat().format(ORDER_TIME_FORMATTER) : "không rõ";
            return "Đơn " + found.getMaDonHang() + " hiện ở trạng thái " + found.getTrangThaiDonHang()
                    + ", đặt lúc " + orderTime + ".";
        }

        if (normalized.contains("ban chay") || normalized.contains("noi bat")) {
            List<ProductChatDto> bestSelling = chatShopQueryRepository.getBestSellingProducts();
            if (bestSelling.isEmpty()) {
                return "Mình chưa có dữ liệu bán chạy, nhưng bạn có thể xem thêm ở mục sản phẩm mới nhất.";
            }
            return "Top sản phẩm bán chạy: " + formatProductList(bestSelling, 5);
        }

        if (normalized.contains("goi y") || normalized.contains("di choi")) {
            List<ProductChatDto> suggestions = chatShopQueryRepository.searchProducts("ao");
            if (suggestions.isEmpty()) {
                suggestions = chatShopQueryRepository.getBestSellingProducts();
            }
            if (suggestions.isEmpty()) {
                return "Mình chưa có gợi ý phù hợp ngay lúc này, bạn thử mô tả rõ kiểu đồ bạn thích nhé.";
            }
            return "Bạn có thể tham khảo: " + formatProductList(suggestions, 4)
                    + ". Nếu muốn, mình lọc tiếp theo ngân sách hoặc màu sắc cho bạn.";
        }

        if (normalized.contains("mau trang") || normalized.contains("trang khong")) {
            List<ProductChatDto> whiteProducts = chatShopQueryRepository.findProductsByColor("trắng");
            if (whiteProducts.isEmpty()) {
                whiteProducts = chatShopQueryRepository.findProductsByColor("trang");
            }
            if (whiteProducts.isEmpty()) {
                return "Hiện mình chưa thấy sản phẩm màu trắng phù hợp trong kho.";
            }
            return "Mình có các sản phẩm màu trắng: " + formatProductList(whiteProducts, 6);
        }

        Optional<BigDecimal> maxPrice = extractPrice(normalized);
        if (maxPrice.isPresent()) {
            List<ProductChatDto> byPrice = chatShopQueryRepository.findProductsByPrice(maxPrice.get());
            byPrice = byPrice.stream().filter(this::isInStock).toList();
            if (containsKeyword(normalized, "vay") || containsKeyword(normalized, "nu")) {
                byPrice = byPrice.stream().filter(p -> normalize(p.getName()).contains("vay") || normalize(p.getCategory()).contains("nu")).toList();
            }
            if (byPrice.isEmpty()) {
                return "Hiện chưa có sản phẩm phù hợp mức giá này, bạn có thể tăng ngân sách một chút để mình tìm thêm.";
            }
            return "Các sản phẩm phù hợp ngân sách của bạn: " + formatProductList(byPrice, 8);
        }

        if (containsKeyword(normalized, "ton kho") || containsKeyword(normalized, "con hang")) {
            String keyword = extractLikelyProductKeyword(normalized);
            Optional<ProductChatDto> stock = chatShopQueryRepository.checkProductStock(keyword);
            if (stock.isEmpty()) {
                return "Mình chưa tìm thấy sản phẩm bạn hỏi, bạn gửi giúp mình tên sản phẩm cụ thể hơn nhé.";
            }
            ProductChatDto product = stock.get();
            if (product.getTotalStock() > 0) {
                return product.getName() + " hiện còn hàng (" + product.getTotalStock() + " sản phẩm khả dụng).";
            }
            return product.getName() + " hiện đang tạm hết hàng.";
        }

        if (containsKeyword(normalized, "ao so mi")) {
            return answerForSearchKeyword("áo sơ mi");
        }
        if (containsKeyword(normalized, "quan jean") || containsKeyword(normalized, "jean nam")) {
            return answerForSearchKeyword("quần jean nam");
        }
        if (containsKeyword(normalized, "hoodie")) {
            return answerForSearchKeyword("hoodie");
        }

        List<String> candidateKeywords = buildCandidateKeywords(normalized);
        for (String keyword : candidateKeywords) {
            List<ProductChatDto> products = chatShopQueryRepository.searchProducts(keyword);
            if (!products.isEmpty()) {
                return "Mình tìm thấy: " + formatProductList(products, 6);
            }
        }

        if (!candidateKeywords.isEmpty()) {
            return "Hiện tại Aura Shop chưa có sản phẩm \"" + originalMessage + "\". Bạn có thể tham khảo các bộ sưu tập áo thun, sơ mi hoặc quần tây đang có sẵn nhé!";
        }

        if (containsKeyword(normalized, "nam")) {
            List<ProductChatDto> byMaleCategory = chatShopQueryRepository.findProductsByCategory("nam");
            if (!byMaleCategory.isEmpty()) {
                return "Một số sản phẩm nam bạn có thể tham khảo: " + formatProductList(byMaleCategory, 6);
            }
        }
        if (containsKeyword(normalized, "nu")) {
            List<ProductChatDto> byFemaleCategory = chatShopQueryRepository.findProductsByCategory("nữ");
            if (byFemaleCategory.isEmpty()) {
                byFemaleCategory = chatShopQueryRepository.findProductsByCategory("nu");
            }
            if (!byFemaleCategory.isEmpty()) {
                return "Một số sản phẩm nữ bạn có thể tham khảo: " + formatProductList(byFemaleCategory, 6);
            }
        }

        return FALLBACK_SCOPE_MESSAGE;
    }

    private String answerForSearchKeyword(String keyword) {
        List<ProductChatDto> products = chatShopQueryRepository.searchProducts(keyword);
        if (products.isEmpty()) {
            return "Hiện tại shop chưa có sản phẩm phù hợp với từ khóa \"" + keyword + "\".";
        }
        return "Shop có các sản phẩm phù hợp: " + formatProductList(products, 6);
    }

    private boolean isOutOfScope(String normalized) {
        return normalized.contains("viet code")
                || normalized.contains("chinh tri")
                || normalized.contains("bong da")
                || normalized.contains("thoi tiet")
                || normalized.contains("hack")
                || normalized.contains("sql");
    }

    private boolean isGreeting(String normalized) {
        return normalized.equals("xin chao")
                || normalized.equals("chao")
                || normalized.equals("hello")
                || normalized.equals("hi")
                || normalized.equals("hey");
    }

    private boolean isAddressQuestion(String normalized) {
        return normalized.contains("dia chi")
                || normalized.contains("o dau")
                || normalized.contains("cua hang o dau")
                || normalized.contains("shop o dau");
    }

    private String askOpenAi(String userMessage, String fallbackAnswer) {
        String systemPrompt = """
                Bạn là một stylist / chuyên gia tư vấn thời trang cực kỳ thân thiện cho website Aura Shop.
                Nhiệm vụ: Giải đáp câu hỏi của khách, gợi ý cách phối đồ, tư vấn phong cách, size số.
                Yêu cầu: Trả lời thật ngắn gọn, súc tích (tối đa 2 - 3 câu ngắn), không lê thê dài dòng. Đi thẳng vào trọng tâm câu hỏi.
                Dữ liệu thực tế từ shop: %s.
                Nếu khách hỏi về chủ đề thời trang chung, hãy thoải mái tư vấn nhanh gọn, duyên dáng.
                """.formatted(fallbackAnswer);

        String combinedPrompt = systemPrompt + "\n\nCâu hỏi từ khách hàng: " + userMessage;

        Map<String, Object> requestBody = new LinkedHashMap<>();
        Map<String, Object> textPart = Map.of("text", combinedPrompt);
        Map<String, Object> contentNode = Map.of(
                "role", "user",
                "parts", List.of(textPart)
        );
        requestBody.put("contents", List.of(contentNode));
        requestBody.put("generationConfig", Map.of("temperature", 0.3));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + openAiModel + ":generateContent?key=" + openAiApiKey;

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(
                url,
                entity,
                Map.class
        );

        if (response == null) {
            return "";
        }

        try {
            Object candidatesObj = response.get("candidates");
            if (!(candidatesObj instanceof List<?> candidates) || candidates.isEmpty()) {
                return "";
            }
            Object firstCandidate = candidates.get(0);
            if (!(firstCandidate instanceof Map<?, ?> candMap)) {
                return "";
            }
            Object contentObj = candMap.get("content");
            if (!(contentObj instanceof Map<?, ?> contentMap)) {
                return "";
            }
            Object partsObj = contentMap.get("parts");
            if (!(partsObj instanceof List<?> parts) || parts.isEmpty()) {
                return "";
            }
            Object firstPart = parts.get(0);
            if (!(firstPart instanceof Map<?, ?> partMap)) {
                return "";
            }
            Object textObj = partMap.get("text");
            return textObj instanceof String text ? text.trim() : "";
        } catch (Exception e) {
            return "";
        }
    }
    
    private boolean isOpenAiConfigured() {
        return openAiApiKey != null && !openAiApiKey.isBlank();
    }

    private boolean isOrderQuestion(String normalized) {
        return normalized.contains("don hang") || normalized.contains("ma don");
    }

    private Optional<String> extractOrderCode(String message) {
        Matcher matcher = ORDER_PATTERN.matcher(message);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group(1));
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> extractPrice(String normalized) {
        Matcher matcher = PRICE_PATTERN.matcher(normalized);
        while (matcher.find()) {
            String numberPart = matcher.group(1);
            String unit = matcher.group(2);
            try {
                String normalizedNumber = numberPart.replace(",", ".");
                BigDecimal value = new BigDecimal(normalizedNumber);
                if (unit != null) {
                    String u = unit.toLowerCase(Locale.ROOT);
                    if (u.equals("k") || u.equals("nghìn") || u.equals("nghin")) {
                        value = value.multiply(BigDecimal.valueOf(1000));
                    } else if (u.equals("tr") || u.equals("triệu") || u.equals("trieu")) {
                        value = value.multiply(BigDecimal.valueOf(1_000_000));
                    }
                } else if (value.compareTo(BigDecimal.valueOf(1000)) <= 0) {
                    value = value.multiply(BigDecimal.valueOf(1000));
                }
                return Optional.of(value);
            } catch (NumberFormatException ignored) {
                // Skip invalid number
            }
        }
        return Optional.empty();
    }

    private String extractLikelyProductKeyword(String normalized) {
        String cleaned = normalized
                .replace("shop co", "")
                .replace("co", "")
                .replace("khong", "")
                .replace("san pham", "")
                .replace("nao", "")
                .replace("duoi", "")
                .replace("gia", "")
                .replace("ton kho", "")
                .replace("con hang", "")
                .trim();
        if (cleaned.length() > 60) {
            cleaned = cleaned.substring(0, 60);
        }
        return cleaned;
    }

    private List<String> buildCandidateKeywords(String normalized) {
        java.util.LinkedHashSet<String> keywords = new java.util.LinkedHashSet<>();

        String likely = extractLikelyProductKeyword(normalized);
        if (!likely.isBlank()) {
            keywords.add(likely);
        }

        String[] commonProductWords = {"quan", "ao", "vay", "hoodie", "jean", "so mi", "thun", "khoac"};
        for (String word : commonProductWords) {
            if (normalized.contains(word)) {
                keywords.add(word);
            }
        }

        String compact = normalized.replace("toi muon mua", "")
                .replace("hay ho tro cho toi kiem", "")
                .replace("hay ho tro toi tim", "")
                .replace("cho toi", "")
                .replace("toi", "")
                .replace("mua", "")
                .replace("tim", "")
                .replace("kiem", "")
                .trim();
        if (!compact.isBlank()) {
            keywords.add(compact);
        }

        return keywords.stream().filter(k -> !k.isBlank()).toList();
    }

    private String formatProductList(List<ProductChatDto> products, int maxItems) {
        return products.stream()
                .limit(maxItems)
                .map(p -> p.getName() + " (" + money(p.getPrice()) + ", tồn: " + p.getTotalStock() + ")")
                .reduce((a, b) -> a + "; " + b)
                .orElse("chưa có dữ liệu");
    }

    private String money(BigDecimal value) {
        if (value == null) {
            return "0đ";
        }
        return String.format("%,.0fđ", value.doubleValue());
    }

    private boolean isInStock(ProductChatDto product) {
        return product.getTotalStock() > 0;
    }

    private boolean containsKeyword(String normalized, String keyword) {
        return normalized.contains(normalize(keyword));
    }

    private String normalize(String input) {
        String noAccent = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noAccent.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
    }
}
