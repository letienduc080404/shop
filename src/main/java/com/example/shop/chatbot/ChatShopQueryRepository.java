package com.example.shop.chatbot;

import com.example.shop.entity.Order;
import com.example.shop.entity.Product;
import com.example.shop.entity.enums.TrangThaiDonHang;
import com.example.shop.repository.CategoryRepository;
import com.example.shop.repository.OrderItemRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.ProductVariantRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class ChatShopQueryRepository {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public ChatShopQueryRepository(ProductRepository productRepository,
                                   CategoryRepository categoryRepository,
                                   ProductVariantRepository productVariantRepository,
                                   OrderRepository orderRepository,
                                   OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productVariantRepository = productVariantRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public List<ProductChatDto> searchProducts(String keyword) {
        String safeKeyword = safe(keyword);
        if (safeKeyword.isBlank()) {
            return List.of();
        }
        List<Product> products = productRepository.findByTenSanPhamContainingIgnoreCase(safeKeyword);
        return toProductDtos(products);
    }

    public List<ProductChatDto> findProductsByCategory(String category) {
        String safeCategory = safe(category).toLowerCase(Locale.ROOT);
        if (safeCategory.isBlank()) {
            return List.of();
        }

        List<Long> categoryIds = categoryRepository.findAll().stream()
                .filter(c -> c.getTenDanhMuc() != null && c.getTenDanhMuc().toLowerCase(Locale.ROOT).contains(safeCategory))
                .map(c -> c.getIdDanhMuc())
                .toList();

        List<Product> products = new ArrayList<>();
        for (Long id : categoryIds) {
            products.addAll(productRepository.findByCategory_IdDanhMuc(id));
        }
        return toProductDtos(products);
    }

    public List<ProductChatDto> findProductsByPrice(BigDecimal maxPrice) {
        if (maxPrice == null) {
            return List.of();
        }
        List<Product> matched = productRepository.findAll().stream()
                .filter(p -> p.getGiaNiemYet() != null && p.getGiaNiemYet().compareTo(maxPrice) <= 0)
                .sorted(Comparator.comparing(Product::getGiaNiemYet))
                .limit(20)
                .toList();
        return toProductDtos(matched);
    }

    public Optional<ProductChatDto> checkProductStock(String productName) {
        List<ProductChatDto> products = searchProducts(productName);
        if (!products.isEmpty()) {
            return Optional.of(products.get(0));
        }
        return Optional.empty();
    }

    public Optional<Order> getOrderStatus(String orderCode) {
        String safeCode = safe(orderCode).toUpperCase(Locale.ROOT);
        if (safeCode.isBlank()) {
            return Optional.empty();
        }
        return orderRepository.findByMaDonHang(safeCode);
    }

    public List<ProductChatDto> getBestSellingProducts() {
        List<OrderItemRepository.ProductSalesAggView> salesRows = orderItemRepository.topProductsByRevenueDonHangNot(
                java.time.LocalDateTime.now().minusYears(3),
                java.time.LocalDateTime.now().plusDays(1),
                TrangThaiDonHang.DaHuy,
                PageRequest.of(0, 5)
        );
        if (salesRows.isEmpty()) {
            return toProductDtos(productRepository.findTop3ByOrderByIdSanPhamDesc());
        }

        Map<Long, Product> productMap = productRepository.findAllById(
                salesRows.stream().map(OrderItemRepository.ProductSalesAggView::getProductId).toList()
        ).stream().collect(Collectors.toMap(Product::getIdSanPham, p -> p));

        List<Product> sortedProducts = new ArrayList<>();
        for (OrderItemRepository.ProductSalesAggView row : salesRows) {
            Product p = productMap.get(row.getProductId());
            if (p != null) {
                sortedProducts.add(p);
            }
        }
        return toProductDtos(sortedProducts);
    }

    public List<ProductChatDto> findProductsByColor(String colorKeyword) {
        String safeColor = safe(colorKeyword).toLowerCase(Locale.ROOT);
        if (safeColor.isBlank()) {
            return List.of();
        }

        Set<Long> productIds = productVariantRepository.findAll().stream()
                .filter(v -> v.getMauSac() != null && v.getMauSac().toLowerCase(Locale.ROOT).contains(safeColor))
                .map(v -> v.getProduct().getIdSanPham())
                .collect(Collectors.toSet());

        return toProductDtos(productRepository.findAllById(productIds));
    }

    public long countProducts() {
        return productRepository.count();
    }

    private List<ProductChatDto> toProductDtos(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return List.of();
        }

        List<Long> productIds = products.stream()
                .map(Product::getIdSanPham)
                .filter(java.util.Objects::nonNull)
                .toList();

        Map<Long, Integer> stockMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            stockMap = productVariantRepository.sumStockByProductIds(productIds).stream()
                    .collect(Collectors.toMap(
                            ProductVariantRepository.ProductStockView::getProductId,
                            ProductVariantRepository.ProductStockView::getTotalStock
                    ));
        }

        Map<Long, Product> unique = new HashMap<>();
        for (Product product : products) {
            unique.putIfAbsent(product.getIdSanPham(), product);
        }

        List<ProductChatDto> dtos = new ArrayList<>();
        for (Product product : unique.values()) {
            String category = product.getCategory() != null ? product.getCategory().getTenDanhMuc() : "Khác";
            int stock = stockMap.getOrDefault(product.getIdSanPham(), 0);
            dtos.add(new ProductChatDto(
                    product.getIdSanPham(),
                    product.getTenSanPham(),
                    category,
                    product.getGiaNiemYet(),
                    stock
            ));
        }
        dtos.sort(Comparator.comparing(ProductChatDto::getName));
        return dtos;
    }

    private String safe(String input) {
        return input == null ? "" : input.trim();
    }
}
