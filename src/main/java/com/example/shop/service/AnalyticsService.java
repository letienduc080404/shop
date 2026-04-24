package com.example.shop.service;

import com.example.shop.dto.admin.AnalyticsSummaryDto;
import com.example.shop.dto.admin.TopProductAnalyticsRowDto;
import com.example.shop.entity.Product;
import com.example.shop.entity.enums.TrangThaiDonHang;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderItemRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AnalyticsService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public AnalyticsService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Tính dữ liệu analytics theo kỳ báo cáo.
     * - from/to là ngày (LocalDate). Backend quy đổi về LocalDateTime [from, toNextDay) để query.
     */
    public AnalyticsSummaryDto buildAnalytics(LocalDate from, LocalDate to) {
        LocalDate safeFrom = (from != null) ? from : YearMonth.now().atDay(1);
        LocalDate safeTo = (to != null) ? to : YearMonth.now().atEndOfMonth();

        LocalDateTime fromDt = safeFrom.atStartOfDay();
        LocalDateTime toDt = safeTo.plusDays(1).atStartOfDay(); // loại trừ toDt, nên +1 ngày

        // Kỳ trước: lấy cùng số ngày ngay trước kỳ hiện tại
        long days = Math.max(1, safeFrom.until(safeTo).getDays() + 1L);
        LocalDate prevTo = safeFrom.minusDays(1);
        LocalDate prevFrom = prevTo.minusDays(days - 1);
        LocalDateTime prevFromDt = prevFrom.atStartOfDay();
        LocalDateTime prevToDt = prevTo.plusDays(1).atStartOfDay();

        BigDecimal doanhThu = orderRepository.sumTongTienByNgayDatBetweenAndTrangThai(fromDt, toDt, TrangThaiDonHang.HoanThanh);
        BigDecimal doanhThuPrev = orderRepository.sumTongTienByNgayDatBetweenAndTrangThai(prevFromDt, prevToDt, TrangThaiDonHang.HoanThanh);

        TrendView doanhThuTrend = calcTrendPercent(doanhThu, doanhThuPrev);

        // Tổng giao dịch: toàn bộ đơn trong kỳ (trừ huỷ)
        long tongGiaoDich = orderRepository.countByNgayDatBetweenAndTrangThaiDonHangNot(fromDt, toDt, TrangThaiDonHang.DaHuy);

        // Đơn hàng mới: các đơn trong 7 ngày gần nhất của kỳ (trừ huỷ) để giống ý “mới”
        LocalDateTime last7From = toDt.minusDays(7);
        long donMoi7Ngay = orderRepository.countByNgayDatBetweenAndTrangThaiDonHangNot(last7From, toDt, TrangThaiDonHang.DaHuy);

        // AOV: doanh thu / số đơn hoàn thành trong kỳ
        long soDonHoanThanh = orderRepository.countByNgayDatBetweenAndTrangThaiDonHangNot(fromDt, toDt, TrangThaiDonHang.DaHuy);
        BigDecimal aov = BigDecimal.ZERO;
        if (soDonHoanThanh > 0) {
            aov = safeBig(doanhThu).divide(BigDecimal.valueOf(soDonHoanThanh), 0, RoundingMode.HALF_UP);
        }

        // Tỉ lệ chuyển đổi (theo dữ liệu DB hiện có): số khách có đơn trong kỳ / tổng khách
        long tongKhach = customerRepository.count();
        long khachCoDon = orderRepository.countDistinctCustomersByNgayDatBetween(fromDt, toDt);
        BigDecimal tiLe = calcRatePercent(khachCoDon, tongKhach);

        long khachCoDonPrev = orderRepository.countDistinctCustomersByNgayDatBetween(prevFromDt, prevToDt);
        BigDecimal tiLePrev = calcRatePercent(khachCoDonPrev, tongKhach);
        TrendView tiLeTrend = calcTrendPercent(tiLe, tiLePrev);

        // Series 7 ngày gần nhất: so sánh 7 ngày cuối kỳ vs 7 ngày trước đó
        List<BigDecimal> seriesNow = buildDailyRevenueSeries(toDt.minusDays(7), toDt);
        List<BigDecimal> seriesPrev = buildDailyRevenueSeries(toDt.minusDays(14), toDt.minusDays(7));
        SvgSeriesView svg = buildSvgPaths(seriesNow, seriesPrev);

        // Top sản phẩm theo doanh thu trong kỳ (top 3)
        List<TopProductAnalyticsRowDto> topProducts = buildTopProducts(fromDt, toDt);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM", Locale.forLanguageTag("vi-VN"));
        String kyBaoCaoLabel = safeFrom.format(fmt) + " - " + safeTo.format(fmt) + "/" + safeTo.getYear();

        return new AnalyticsSummaryDto(
                kyBaoCaoLabel,
                formatMoneyVnd(doanhThu),
                doanhThuTrend.text,
                doanhThuTrend.css,
                formatPercent2(tiLe) + "%",
                tiLeTrend.text,
                tiLeTrend.css,
                formatCount(donMoi7Ngay) + " ĐƠN",
                formatCount(tongGiaoDich),
                formatMoneyVnd(aov),
                svg.pathNow,
                svg.pathPrev,
                svg.tooltipText,
                svg.tooltipLeftCss,
                topProducts
        );
    }

    private List<BigDecimal> buildDailyRevenueSeries(LocalDateTime from, LocalDateTime to) {
        List<BigDecimal> result = new ArrayList<>();
        LocalDateTime cur = from;
        while (cur.isBefore(to)) {
            LocalDateTime next = cur.plusDays(1);
            BigDecimal v = orderRepository.sumTongTienByNgayDatBetweenAndTrangThai(cur, next, TrangThaiDonHang.HoanThanh);
            result.add(safeBig(v));
            cur = next;
        }
        return result;
    }

    private List<TopProductAnalyticsRowDto> buildTopProducts(LocalDateTime from, LocalDateTime to) {
        var rows = orderItemRepository.topProductsByRevenue(from, to, TrangThaiDonHang.HoanThanh, PageRequest.of(0, 3));
        List<Long> ids = rows.stream().map(OrderItemRepository.ProductSalesAggView::getProductId).toList();
        Map<Long, Product> productMap = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Product p : productRepository.findAllById(ids)) {
                productMap.put(p.getIdSanPham(), p);
            }
        }

        // Xếp hạng hiệu suất đơn giản theo thứ tự doanh thu
        List<TopProductAnalyticsRowDto> result = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            var r = rows.get(i);
            Product p = productMap.get(r.getProductId());
            String ten = (p != null) ? p.getTenSanPham() : "Sản phẩm";
            String dm = (p != null && p.getCategory() != null) ? p.getCategory().getTenDanhMuc() : "Khác";
            String img = (p != null) ? p.getImageUrl() : "";
            String gia = (p != null) ? formatMoneyVnd(p.getGiaNiemYet()) : "0đ";
            long daBan = (r.getDaBan() == null) ? 0 : r.getDaBan();
            String doanhThu = formatCompactVnd(r.getDoanhThu()) + " ₫";

            HieuSuatView hs = switch (i) {
                case 0 -> new HieuSuatView("CAO NHẤT", "bg-secondary/10 text-secondary");
                case 1 -> new HieuSuatView("ỔN ĐỊNH", "bg-primary/10 text-primary");
                default -> new HieuSuatView("TIỀM NĂNG", "bg-tertiary/10 text-tertiary");
            };

            result.add(new TopProductAnalyticsRowDto(
                    ten,
                    "Collections: " + dm,
                    img,
                    gia,
                    daBan,
                    doanhThu,
                    hs.label,
                    hs.css
            ));
        }

        // Fallback: nếu kỳ báo cáo chưa phát sinh đơn (bảng trống), vẫn hiển thị sản phẩm thật (0 đã bán, 0 doanh thu)
        if (result.isEmpty()) {
            List<Product> latest = productRepository.findTop3ByOrderByIdSanPhamDesc();
            for (int i = 0; i < latest.size(); i++) {
                Product p = latest.get(i);
                String ten = (p != null) ? p.getTenSanPham() : "Sản phẩm";
                String dm = (p != null && p.getCategory() != null) ? p.getCategory().getTenDanhMuc() : "Khác";
                String img = (p != null) ? p.getImageUrl() : "";
                String gia = (p != null) ? formatMoneyVnd(p.getGiaNiemYet()) : "0 ₫";

                HieuSuatView hs = switch (i) {
                    case 0 -> new HieuSuatView("CHƯA CÓ ĐƠN", "bg-outline-variant/20 text-on-surface-variant");
                    case 1 -> new HieuSuatView("CHƯA CÓ ĐƠN", "bg-outline-variant/20 text-on-surface-variant");
                    default -> new HieuSuatView("CHƯA CÓ ĐƠN", "bg-outline-variant/20 text-on-surface-variant");
                };

                result.add(new TopProductAnalyticsRowDto(
                        ten,
                        "Collections: " + dm,
                        img,
                        gia,
                        0,
                        "0 ₫",
                        hs.label,
                        hs.css
                ));
            }
        }
        return result;
    }

    private static class HieuSuatView {
        private final String label;
        private final String css;

        private HieuSuatView(String label, String css) {
            this.label = label;
            this.css = css;
        }
    }

    private static class TrendView {
        private final String text;
        private final String css;

        private TrendView(String text, String css) {
            this.text = text;
            this.css = css;
        }
    }

    /**
     * Tính % thay đổi giữa kỳ hiện tại và kỳ trước.
     * - Nếu kỳ trước = 0: tránh chia 0.
     */
    private TrendView calcTrendPercent(BigDecimal current, BigDecimal previous) {
        BigDecimal cur = safeBig(current);
        BigDecimal prev = safeBig(previous);

        if (prev.compareTo(BigDecimal.ZERO) == 0) {
            if (cur.compareTo(BigDecimal.ZERO) == 0) {
                return new TrendView("+0.0%", "text-on-surface-variant");
            }
            return new TrendView("+100.0%", "text-secondary");
        }

        BigDecimal delta = cur.subtract(prev)
                .multiply(BigDecimal.valueOf(100))
                .divide(prev, 1, RoundingMode.HALF_UP);

        String sign = delta.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        String text = sign + delta.toPlainString() + "%";
        String css = delta.compareTo(BigDecimal.ZERO) >= 0 ? "text-secondary" : "text-secondary";
        return new TrendView(text, css);
    }

    private BigDecimal calcRatePercent(long numerator, long denominator) {
        if (denominator <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private String formatPercent2(BigDecimal value) {
        return safeBig(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private BigDecimal safeBig(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private String formatMoneyVnd(BigDecimal money) {
        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        nf.setMaximumFractionDigits(0);
        BigDecimal v = safeBig(money);
        return nf.format(v) + " ₫";
    }

    private String formatCount(long value) {
        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        nf.setMaximumFractionDigits(0);
        return nf.format(value);
    }

    /**
     * Rút gọn tiền VND theo K/M/B để hiển thị gọn (vd: 639M).
     */
    private String formatCompactVnd(BigDecimal vnd) {
        BigDecimal v = safeBig(vnd).abs();
        BigDecimal oneB = BigDecimal.valueOf(1_000_000_000L);
        BigDecimal oneM = BigDecimal.valueOf(1_000_000L);
        BigDecimal oneK = BigDecimal.valueOf(1_000L);

        if (v.compareTo(oneB) >= 0) {
            return v.divide(oneB, 0, RoundingMode.HALF_UP).toPlainString() + "B";
        }
        if (v.compareTo(oneM) >= 0) {
            return v.divide(oneM, 0, RoundingMode.HALF_UP).toPlainString() + "M";
        }
        if (v.compareTo(oneK) >= 0) {
            return v.divide(oneK, 0, RoundingMode.HALF_UP).toPlainString() + "K";
        }
        return v.toPlainString();
    }

    private static class SvgSeriesView {
        private final String pathNow;
        private final String pathPrev;
        private final String tooltipText;
        private final String tooltipLeftCss;

        private SvgSeriesView(String pathNow, String pathPrev, String tooltipText, String tooltipLeftCss) {
            this.pathNow = pathNow;
            this.pathPrev = pathPrev;
            this.tooltipText = tooltipText;
            this.tooltipLeftCss = tooltipLeftCss;
        }
    }

    /**
     * Dựng SVG path cho 2 series (7 điểm).
     * ViewBox: 0..1000 (x), 0..200 (y). Ta vẽ trong vùng y [30..160] để đẹp.
     */
    private SvgSeriesView buildSvgPaths(List<BigDecimal> seriesNow, List<BigDecimal> seriesPrev) {
        int n = Math.min(seriesNow.size(), seriesPrev.size());
        if (n <= 1) {
            return new SvgSeriesView("M0 150 L1000 150", "M0 160 L1000 160", "", "left-1/2");
        }

        BigDecimal max = BigDecimal.ZERO;
        for (int i = 0; i < n; i++) {
            max = max.max(seriesNow.get(i)).max(seriesPrev.get(i));
        }
        if (max.compareTo(BigDecimal.ZERO) == 0) {
            return new SvgSeriesView("M0 150 L1000 150", "M0 160 L1000 160", "", "left-1/2");
        }

        double xStep = 1000.0 / (n - 1);
        double yTop = 30.0;
        double yBot = 160.0;

        String pathNow = buildPath(seriesNow, max, xStep, yTop, yBot);
        String pathPrev = buildPath(seriesPrev, max, xStep, yTop, yBot);

        // Tooltip: lấy ngày có doanh thu cao nhất của seriesNow
        int idxMax = 0;
        for (int i = 1; i < n; i++) {
            if (seriesNow.get(i).compareTo(seriesNow.get(idxMax)) > 0) idxMax = i;
        }
        String tooltipText = "Ngày " + (idxMax + 1) + ": " + formatCompactVnd(seriesNow.get(idxMax)) + " ₫";

        int leftPercent = (int) Math.round((idxMax * 100.0) / (n - 1));
        leftPercent = Math.max(10, Math.min(90, leftPercent));
        String tooltipLeftCss = "left-[" + leftPercent + "%]";

        return new SvgSeriesView(pathNow, pathPrev, tooltipText, tooltipLeftCss);
    }

    private String buildPath(List<BigDecimal> series, BigDecimal max, double xStep, double yTop, double yBot) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < series.size(); i++) {
            double x = i * xStep;
            double ratio = series.get(i).divide(max, 6, RoundingMode.HALF_UP).doubleValue();
            double y = yBot - (yBot - yTop) * ratio;
            if (i == 0) sb.append("M").append(fmt(x)).append(" ").append(fmt(y));
            else sb.append(" L").append(fmt(x)).append(" ").append(fmt(y));
        }
        return sb.toString();
    }

    private String fmt(double v) {
        return String.format(Locale.US, "%.2f", v);
    }
}

