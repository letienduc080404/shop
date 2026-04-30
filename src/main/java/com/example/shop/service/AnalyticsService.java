package com.example.shop.service;

import com.example.shop.dto.admin.AnalyticsSummaryDto;
import com.example.shop.dto.admin.TopProductAnalyticsRowDto;
import com.example.shop.entity.Product;
import com.example.shop.entity.enums.TrangThaiDonHang;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderItemRepository;
import com.example.shop.repository.ProductRepository;
import com.example.shop.util.FormatUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AnalyticsService {
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final StatisticsService statisticsService;

    public AnalyticsService(OrderItemRepository orderItemRepository,
                            ProductRepository productRepository, CustomerRepository customerRepository,
                            StatisticsService statisticsService) {
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.statisticsService = statisticsService;
    }

    public AnalyticsSummaryDto buildAnalytics(LocalDate from, LocalDate to) {
        // Nếu không có 'from'/'to' từ request, dùng mặc định năm nay
        int year = (from != null) ? from.getYear() : LocalDate.now().getYear();
        
        LocalDateTime startYear = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endYear = LocalDateTime.of(year, 12, 31, 23, 59);

        LocalDateTime prevStartYear = startYear.minusYears(1);
        LocalDateTime prevEndYear = endYear.minusYears(1);

        BigDecimal doanhThu = statisticsService.sumRevenueBetween(startYear, endYear);
        BigDecimal doanhThuPrev = statisticsService.sumRevenueBetween(prevStartYear, prevEndYear);
        TrendView doanhThuTrend = calcTrendPercent(doanhThu, doanhThuPrev);

        long tongGiaoDich = statisticsService.countOrdersBetween(startYear, endYear);
        
        // Đơn mới tháng cuối (Tháng 12 hoặc tháng hiện tại)
        int lastMonth = (year == LocalDate.now().getYear()) ? LocalDate.now().getMonthValue() : 12;
        long donMoiThangCuoi = statisticsService.countOrdersBetween(
                LocalDateTime.of(year, lastMonth, 1, 0, 0), endYear);

        BigDecimal aov = tongGiaoDich > 0 ? doanhThu.divide(BigDecimal.valueOf(tongGiaoDich), 0, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        long tongKhach = customerRepository.count();
        long khachCoDon = statisticsService.countUniqueCustomersBetween(startYear, endYear);
        BigDecimal tiLe = calcRatePercent(khachCoDon, tongKhach);
        BigDecimal tiLePrev = calcRatePercent(statisticsService.countUniqueCustomersBetween(prevStartYear, prevEndYear), tongKhach);
        TrendView tiLeTrend = calcTrendPercent(tiLe, tiLePrev);

        // Biểu đồ xu hướng 12 tháng (Bulk query)
        Map<Integer, BigDecimal> monthlyNow = statisticsService.getMonthlyRevenueForYear(year);
        Map<Integer, BigDecimal> monthlyPrev = statisticsService.getMonthlyRevenueForYear(year - 1);
        
        List<BigDecimal> seriesNow = new ArrayList<>();
        List<BigDecimal> seriesPrev = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            seriesNow.add(monthlyNow.getOrDefault(i, BigDecimal.ZERO));
            seriesPrev.add(monthlyPrev.getOrDefault(i, BigDecimal.ZERO));
        }

        SvgSeriesView svg = buildSvgPaths(seriesNow, seriesPrev);
        List<TopProductAnalyticsRowDto> topProducts = buildTopProducts(startYear, endYear);

        return new AnalyticsSummaryDto(
                "Năm " + year, FormatUtils.formatMoneyVnd(doanhThu), doanhThuTrend.text, doanhThuTrend.css,
                FormatUtils.formatPercent(tiLe, 2) + "%", tiLeTrend.text, tiLeTrend.css,
                FormatUtils.formatCount(donMoiThangCuoi) + " ĐƠN", FormatUtils.formatCount(tongGiaoDich), FormatUtils.formatMoneyVnd(aov),
                svg.pathNow, svg.pathPrev, svg.tooltipText, svg.tooltipLeftCss, topProducts
        );
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

            long daBan = Optional.ofNullable(r.getDaBan()).orElse(0L);
            result.add(new TopProductAnalyticsRowDto(p.getTenSanPham(), "Collections: " + dm, p.getImageUrl(),
                    FormatUtils.formatMoneyVnd(p.getGiaNiemYet()), daBan, 
                    FormatUtils.formatCompactVnd(r.getDoanhThu()) + " ₫", hieuSuat, hsCss));
        }

        if (result.isEmpty()) {
            productRepository.findTop3ByOrderByIdSanPhamDesc().forEach(p -> {
                result.add(new TopProductAnalyticsRowDto(p.getTenSanPham(), "Collections: " + (p.getCategory() != null ? p.getCategory().getTenDanhMuc() : "Khác"),
                        p.getImageUrl(), FormatUtils.formatMoneyVnd(p.getGiaNiemYet()), 0, "0 ₫", "CHƯA CÓ ĐƠN", "bg-outline-variant/20 text-on-surface-variant"));
            });
        }
        return result;
    }

    private record TrendView(String text, String css) {}
    private record SvgSeriesView(String pathNow, String pathPrev, String tooltipText, String tooltipLeftCss) {}

    private TrendView calcTrendPercent(BigDecimal current, BigDecimal previous) {
        BigDecimal cur = current == null ? BigDecimal.ZERO : current;
        BigDecimal prev = previous == null ? BigDecimal.ZERO : previous;
        if (prev.compareTo(BigDecimal.ZERO) == 0) return new TrendView(cur.compareTo(BigDecimal.ZERO) == 0 ? "+0.0%" : "+100.0%", "text-on-surface-variant");
        BigDecimal delta = cur.subtract(prev).multiply(BigDecimal.valueOf(100)).divide(prev, 1, RoundingMode.HALF_UP);
        return new TrendView((delta.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + delta.toPlainString() + "%", "text-secondary");
    }

    private BigDecimal calcRatePercent(long num, long den) {
        if (den <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(num).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(den), 2, RoundingMode.HALF_UP);
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
        String tooltipText = "Tháng " + (idxMax + 1) + ": " + FormatUtils.formatCompactVnd(seriesNow.get(idxMax)) + " ₫";

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
