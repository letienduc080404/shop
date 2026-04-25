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
import java.util.*;

@Service
public class AnalyticsService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public AnalyticsService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                            ProductRepository productRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    public AnalyticsSummaryDto buildAnalytics(LocalDate from, LocalDate to) {
        // CỐ ĐỊNH NĂM 2025 CHO TRANG ANALYTICS
        LocalDateTime fromDt = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime toDt = LocalDateTime.of(2025, 12, 31, 23, 59);

        // Năm 2024 làm mốc so sánh
        LocalDateTime prevFromDt = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime prevToDt = LocalDateTime.of(2024, 12, 31, 23, 59);

        BigDecimal doanhThu = safeBig(orderRepository.sumTongTienByNgayDatBetweenAndTrangThaiDonHangNot(fromDt, toDt, TrangThaiDonHang.DaHuy));
        BigDecimal doanhThuPrev = safeBig(orderRepository.sumTongTienByNgayDatBetweenAndTrangThaiDonHangNot(prevFromDt, prevToDt, TrangThaiDonHang.DaHuy));
        TrendView doanhThuTrend = calcTrendPercent(doanhThu, doanhThuPrev);

        long tongGiaoDich = orderRepository.countByNgayDatBetweenAndTrangThaiDonHangNot(fromDt, toDt, TrangThaiDonHang.DaHuy);
        long donMoiThangCuoi = orderRepository.countByNgayDatBetweenAndTrangThaiDonHangNot(LocalDateTime.of(2025,12,1,0,0), toDt, TrangThaiDonHang.DaHuy);

        BigDecimal aov = tongGiaoDich > 0 ? doanhThu.divide(BigDecimal.valueOf(tongGiaoDich), 0, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        long tongKhach = customerRepository.count();
        long khachCoDon = orderRepository.countDistinctCustomersByNgayDatBetween(fromDt, toDt);
        BigDecimal tiLe = calcRatePercent(khachCoDon, tongKhach);
        BigDecimal tiLePrev = calcRatePercent(orderRepository.countDistinctCustomersByNgayDatBetween(prevFromDt, prevToDt), tongKhach);
        TrendView tiLeTrend = calcTrendPercent(tiLe, tiLePrev);

        // Biểu đồ xu hướng 12 tháng năm 2025 vs 2024
        List<BigDecimal> seriesNow = buildMonthlySeries(2025);
        List<BigDecimal> seriesPrev = buildMonthlySeries(2024);
        SvgSeriesView svg = buildSvgPaths(seriesNow, seriesPrev);

        List<TopProductAnalyticsRowDto> topProducts = buildTopProducts(fromDt, toDt);

        return new AnalyticsSummaryDto(
                "Năm 2025", formatMoneyVnd(doanhThu), doanhThuTrend.text, doanhThuTrend.css,
                formatPercent2(tiLe) + "%", tiLeTrend.text, tiLeTrend.css,
                formatCount(donMoiThangCuoi) + " ĐƠN", formatCount(tongGiaoDich), formatMoneyVnd(aov),
                svg.pathNow, svg.pathPrev, svg.tooltipText, svg.tooltipLeftCss, topProducts
        );
    }

    private List<BigDecimal> buildMonthlySeries(int year) {
        List<BigDecimal> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            LocalDateTime start = LocalDateTime.of(year, m, 1, 0, 0);
            LocalDateTime end = start.plusMonths(1);
            result.add(safeBig(orderRepository.sumTongTienByNgayDatBetweenAndTrangThaiDonHangNot(start, end, TrangThaiDonHang.DaHuy)));
        }
        return result;
    }

    private List<TopProductAnalyticsRowDto> buildTopProducts(LocalDateTime from, LocalDateTime to) {
        var rows = orderItemRepository.topProductsByRevenueDonHangNot(from, to, TrangThaiDonHang.DaHuy, PageRequest.of(0, 3));
        List<TopProductAnalyticsRowDto> result = new ArrayList<>();
        
        for (int i = 0; i < rows.size(); i++) {
            var r = rows.get(i);
            Product p = productRepository.findById(r.getProductId()).orElse(null);
            if (p == null) continue;
            
            String dm = p.getCategory() != null ? p.getCategory().getTenDanhMuc() : "Khác";
            String hieuSuat = i == 0 ? "CAO NHẤT" : (i == 1 ? "ỔN ĐỊNH" : "TIỀM NĂNG");
            String hsCss = i == 0 ? "bg-secondary/10 text-secondary" : (i == 1 ? "bg-primary/10 text-primary" : "bg-tertiary/10 text-tertiary");

            result.add(new TopProductAnalyticsRowDto(p.getTenSanPham(), "Collections: " + dm, p.getImageUrl(),
                    formatMoneyVnd(p.getGiaNiemYet()), r.getDaBan() != null ? r.getDaBan() : 0, 
                    formatCompactVnd(r.getDoanhThu()) + " ₫", hieuSuat, hsCss));
        }

        if (result.isEmpty()) {
            productRepository.findTop3ByOrderByIdSanPhamDesc().forEach(p -> {
                result.add(new TopProductAnalyticsRowDto(p.getTenSanPham(), "Collections: " + (p.getCategory() != null ? p.getCategory().getTenDanhMuc() : "Khác"),
                        p.getImageUrl(), formatMoneyVnd(p.getGiaNiemYet()), 0, "0 ₫", "CHƯA CÓ ĐƠN", "bg-outline-variant/20 text-on-surface-variant"));
            });
        }
        return result;
    }

    private record TrendView(String text, String css) {}
    private record SvgSeriesView(String pathNow, String pathPrev, String tooltipText, String tooltipLeftCss) {}

    private TrendView calcTrendPercent(BigDecimal current, BigDecimal previous) {
        BigDecimal cur = safeBig(current);
        BigDecimal prev = safeBig(previous);
        if (prev.compareTo(BigDecimal.ZERO) == 0) return new TrendView(cur.compareTo(BigDecimal.ZERO) == 0 ? "+0.0%" : "+100.0%", "text-on-surface-variant");
        BigDecimal delta = cur.subtract(prev).multiply(BigDecimal.valueOf(100)).divide(prev, 1, RoundingMode.HALF_UP);
        return new TrendView((delta.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + delta.toPlainString() + "%", "text-secondary");
    }

    private BigDecimal calcRatePercent(long num, long den) {
        if (den <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(num).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(den), 2, RoundingMode.HALF_UP);
    }

    private String formatPercent2(BigDecimal value) { return safeBig(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString(); }
    private BigDecimal safeBig(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private String formatMoneyVnd(BigDecimal money) {
        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        nf.setMaximumFractionDigits(0);
        return nf.format(safeBig(money)) + " ₫";
    }

    private String formatCount(long value) { return NumberFormat.getInstance(Locale.forLanguageTag("vi-VN")).format(value); }

    private String formatCompactVnd(BigDecimal vnd) {
        BigDecimal v = safeBig(vnd).abs();
        if (v.compareTo(BigDecimal.valueOf(1_000_000_000L)) >= 0) return v.divide(BigDecimal.valueOf(1_000_000_000L), 0, RoundingMode.HALF_UP).toPlainString() + "B";
        if (v.compareTo(BigDecimal.valueOf(1_000_000L)) >= 0) return v.divide(BigDecimal.valueOf(1_000_000L), 0, RoundingMode.HALF_UP).toPlainString() + "M";
        if (v.compareTo(BigDecimal.valueOf(1_000L)) >= 0) return v.divide(BigDecimal.valueOf(1_000L), 0, RoundingMode.HALF_UP).toPlainString() + "K";
        return v.toPlainString();
    }

    private SvgSeriesView buildSvgPaths(List<BigDecimal> seriesNow, List<BigDecimal> seriesPrev) {
        int n = seriesNow.size();
        BigDecimal max = BigDecimal.ZERO;
        for (int i = 0; i < n; i++) max = max.max(seriesNow.get(i)).max(seriesPrev.get(i));
        if (max.compareTo(BigDecimal.ZERO) == 0) return new SvgSeriesView("M0 150 L1000 150", "M0 160 L1000 160", "", "left-1/2");

        double xStep = 1000.0 / (n - 1);
        String pathNow = buildPath(seriesNow, max, xStep, 30.0, 160.0);
        String pathPrev = buildPath(seriesPrev, max, xStep, 30.0, 160.0);

        int idxMax = 0;
        for (int i = 1; i < n; i++) if (seriesNow.get(i).compareTo(seriesNow.get(idxMax)) > 0) idxMax = i;
        String tooltipText = "Tháng " + (idxMax + 1) + ": " + formatCompactVnd(seriesNow.get(idxMax)) + " ₫";

        int leftPercent = Math.max(10, Math.min(90, (int) Math.round((idxMax * 100.0) / (n - 1))));
        return new SvgSeriesView(pathNow, pathPrev, tooltipText, "left-[" + leftPercent + "%]");
    }

    private String buildPath(List<BigDecimal> series, BigDecimal max, double xStep, double yTop, double yBot) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < series.size(); i++) {
            double x = i * xStep;
            double ratio = series.get(i).divide(max, 6, RoundingMode.HALF_UP).doubleValue();
            double y = yBot - (yBot - yTop) * ratio;
            sb.append(i == 0 ? "M" : " L").append(String.format(Locale.US, "%.2f %.2f", x, y));
        }
        return sb.toString();
    }
}
