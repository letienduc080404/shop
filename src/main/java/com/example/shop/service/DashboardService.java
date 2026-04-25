package com.example.shop.service;

import com.example.shop.dto.admin.DashboardSummaryDto;
import com.example.shop.dto.admin.MonthlyRevenuePointDto;
import com.example.shop.dto.admin.RecentOrderRowDto;
import com.example.shop.entity.Order;
import com.example.shop.entity.enums.TrangThaiDonHang;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.util.FormatUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DashboardService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final StatisticsService statisticsService;

    public DashboardService(OrderRepository orderRepository, 
                            CustomerRepository customerRepository,
                            StatisticsService statisticsService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.statisticsService = statisticsService;
    }

    public DashboardSummaryDto buildAdminDashboardSummary() {
        LocalDateTime now = LocalDateTime.now();
        // NOTE: Nếu DB cũ, bạn có thể chỉnh fix cứng 'now' tại đây để xem dữ liệu mẫu.

        YearMonth currentMonth = YearMonth.from(now);
        YearMonth previousMonth = currentMonth.minusMonths(1);

        LocalDateTime startCurrentMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime startNextMonth = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
        LocalDateTime startPreviousMonth = previousMonth.atDay(1).atStartOfDay();

        BigDecimal doanhThuThang = statisticsService.sumRevenueBetween(startCurrentMonth, startNextMonth);
        BigDecimal doanhThuThangTruoc = statisticsService.sumRevenueBetween(startPreviousMonth, startCurrentMonth);

        String doanhThuThangText = FormatUtils.formatCompactVnd(doanhThuThang);
        TrendView doanhThuTrend = calcTrendPercent(doanhThuThang, doanhThuThangTruoc);

        long donHangMoi = statisticsService.countOrdersBetween(startCurrentMonth, startNextMonth);
        long donHangMoiThangTruoc = statisticsService.countOrdersBetween(startPreviousMonth, startCurrentMonth);
        TrendView donHangTrend = calcTrendPercent(BigDecimal.valueOf(donHangMoi), BigDecimal.valueOf(donHangMoiThangTruoc));

        String donHangMoiText = FormatUtils.formatCount(donHangMoi);
        long tongKhachHang = customerRepository.count();
        String tongKhachHangText = FormatUtils.formatCompactCount(tongKhachHang);

        BigDecimal tiLeChuyenDoi = calcRatePercent(statisticsService.countUniqueCustomersBetween(now.minusDays(30), now), tongKhachHang);
        BigDecimal tiLeChuyenDoiTruoc = calcRatePercent(statisticsService.countUniqueCustomersBetween(now.minusDays(60), now.minusDays(30)), tongKhachHang);
        TrendView tiLeChuyenDoiTrend = calcTrendPercent(tiLeChuyenDoi, tiLeChuyenDoiTruoc);

        List<MonthlyRevenuePointDto> doanhThu12Thang = buildMonthlySeries(statisticsService.getMonthlyRevenueForYear(now.getYear()));
        List<MonthlyRevenuePointDto> loiNhuan12Thang = buildMonthlySeries(statisticsService.getMonthlyProfitForYear(now.getYear()));

        List<RecentOrderRowDto> recent = mapRecentOrders(orderRepository.findTop5ByOrderByNgayDatDesc());

        return new DashboardSummaryDto(
                doanhThuThangText, doanhThuTrend.text, doanhThuTrend.css,
                donHangMoiText, donHangTrend.text, donHangTrend.css,
                tongKhachHangText, "ỔN ĐỊNH",
                FormatUtils.formatPercent(tiLeChuyenDoi, 1) + "%", tiLeChuyenDoiTrend.text, tiLeChuyenDoiTrend.css,
                doanhThu12Thang, loiNhuan12Thang, recent
        );
    }

    private List<MonthlyRevenuePointDto> buildMonthlySeries(Map<Integer, BigDecimal> monthlyData) {
        BigDecimal max = monthlyData.values().stream().reduce(BigDecimal.ZERO, BigDecimal::max);
        List<MonthlyRevenuePointDto> result = new ArrayList<>();
        
        for (int m = 1; m <= 12; m++) {
            BigDecimal v = monthlyData.get(m);
            int percent = 4;
            if (max.compareTo(BigDecimal.ZERO) > 0) {
                percent = v.multiply(BigDecimal.valueOf(100)).divide(max, 0, RoundingMode.HALF_UP).intValue();
                if (v.compareTo(BigDecimal.ZERO) > 0) percent = Math.max(30, percent);
            }
            result.add(new MonthlyRevenuePointDto("T" + m, v, Math.max(0, Math.min(100, percent))));
        }
        return result;
    }

    private List<RecentOrderRowDto> mapRecentOrders(List<Order> orders) {
        DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("HH:mm - dd/MM", Locale.forLanguageTag("vi-VN"));
        List<RecentOrderRowDto> rows = new ArrayList<>();
        for (Order o : orders) {
            String sanitizedKh = (o.getCustomer() != null) ? (o.getCustomer().getHoTen() != null ? o.getCustomer().getHoTen() : "") : "Không rõ";
            String sanPham = "Đơn hàng Aura";
            if (o.getOrderItems() != null && !o.getOrderItems().isEmpty()) {
                sanPham = (o.getOrderItems().get(0).getProductVariant().getProduct().getTenSanPham() != null ? o.getOrderItems().get(0).getProductVariant().getProduct().getTenSanPham() : "");
                if (o.getOrderItems().size() > 1) sanPham += " (+" + (o.getOrderItems().size() - 1) + ")";
            }
            TrangThaiView status = mapTrangThai(o.getTrangThaiDonHang());
            rows.add(new RecentOrderRowDto("#" + (o.getMaDonHang() != null ? o.getMaDonHang() : ""), sanitizedKh, 
                (o.getNgayDat() != null ? o.getNgayDat().format(dtFmt) : ""), sanPham, FormatUtils.formatMoneyVnd(o.getTongTien()), status.label, status.css));
        }
        return rows;
    }

    private record TrangThaiView(String label, String css) {}

    private TrangThaiView mapTrangThai(TrangThaiDonHang status) {
        if (status == null) return new TrangThaiView("Không rõ", "bg-gray-100 text-gray-700");
        return switch (status) {
            case HoanThanh -> new TrangThaiView("Hoàn thành", "bg-green-100 text-green-700");
            case DangGiao -> new TrangThaiView("Đang giao", "bg-yellow-100 text-yellow-700");
            case ChoXuLy -> new TrangThaiView("Đang xử lý", "bg-yellow-100 text-yellow-700");
            case DaHuy -> new TrangThaiView("Đã huỷ", "bg-red-100 text-secondary");
        };
    }

    private record TrendView(String text, String css) {}

    private TrendView calcTrendPercent(BigDecimal current, BigDecimal previous) {
        BigDecimal cur = current == null ? BigDecimal.ZERO : current;
        BigDecimal prev = previous == null ? BigDecimal.ZERO : previous;
        if (prev.compareTo(BigDecimal.ZERO) == 0) return new TrendView(cur.compareTo(BigDecimal.ZERO) == 0 ? "+0.0%" : "+100.0%", "text-green-600");
        BigDecimal delta = cur.subtract(prev).multiply(BigDecimal.valueOf(100)).divide(prev, 1, RoundingMode.HALF_UP);
        return new TrendView((delta.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + delta.toPlainString() + "%", delta.compareTo(BigDecimal.ZERO) >= 0 ? "text-green-600" : "text-secondary");
    }

    private BigDecimal calcRatePercent(long num, long den) {
        if (den <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(num).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(den), 2, RoundingMode.HALF_UP);
    }
}
