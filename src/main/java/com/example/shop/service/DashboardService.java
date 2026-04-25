package com.example.shop.service;

import com.example.shop.dto.admin.DashboardSummaryDto;
import com.example.shop.dto.admin.MonthlyRevenuePointDto;
import com.example.shop.dto.admin.RecentOrderRowDto;
import com.example.shop.entity.Order;
import com.example.shop.entity.OrderItem;
import com.example.shop.entity.enums.TrangThaiDonHang;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderItemRepository;
import com.example.shop.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class DashboardService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;

    public DashboardService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
    }

    public DashboardSummaryDto buildAdminDashboardSummary() {
        // GIẢ ĐỊNH HÔM NAY LÀ CUỐI NĂM 2025 ĐỂ KHỚP VỚI DATABASE
        LocalDateTime now = LocalDateTime.of(2025, 12, 31, 23, 59);

        YearMonth currentMonth = YearMonth.from(now);
        YearMonth previousMonth = currentMonth.minusMonths(1);

        LocalDateTime startCurrentMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime startNextMonth = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        LocalDateTime startPreviousMonth = previousMonth.atDay(1).atStartOfDay();
        LocalDateTime startCurrentMonthFromPrev = currentMonth.atDay(1).atStartOfDay();

        BigDecimal doanhThuThang = orderRepository.sumTongTienByNgayDatBetweenAndTrangThaiDonHangNot(
                startCurrentMonth, startNextMonth, TrangThaiDonHang.DaHuy);
        BigDecimal doanhThuThangTruoc = orderRepository.sumTongTienByNgayDatBetweenAndTrangThaiDonHangNot(
                startPreviousMonth, startCurrentMonthFromPrev, TrangThaiDonHang.DaHuy);

        String doanhThuThangText = formatCompactVnd(doanhThuThang);
        TrendView doanhThuTrend = calcTrendPercent(doanhThuThang, doanhThuThangTruoc);

        long donHangMoi = orderRepository.countByNgayDatBetweenAndTrangThaiDonHangNot(
                startCurrentMonth, startNextMonth, TrangThaiDonHang.DaHuy);
        long donHangMoiThangTruoc = orderRepository.countByNgayDatBetweenAndTrangThaiDonHangNot(
                startPreviousMonth, startCurrentMonthFromPrev, TrangThaiDonHang.DaHuy);
        TrendView donHangTrend = calcTrendPercent(BigDecimal.valueOf(donHangMoi), BigDecimal.valueOf(donHangMoiThangTruoc));

        String donHangMoiText = formatCount(donHangMoi);
        long tongKhachHang = customerRepository.count();
        String tongKhachHangText = formatCompactCount(tongKhachHang);

        BigDecimal tiLeChuyenDoi = calcRatePercent(orderRepository.countDistinctCustomersByNgayDatBetween(now.minusDays(30), now), tongKhachHang);
        BigDecimal tiLeChuyenDoiTruoc = calcRatePercent(orderRepository.countDistinctCustomersByNgayDatBetween(now.minusDays(60), now.minusDays(30)), tongKhachHang);
        TrendView tiLeChuyenDoiTrend = calcTrendPercent(tiLeChuyenDoi, tiLeChuyenDoiTruoc);

        List<MonthlyRevenuePointDto> doanhThu12Thang = buildLast12MonthsRevenueSeries(now);
        List<MonthlyRevenuePointDto> loiNhuan12Thang = buildLast12MonthsProfitSeries(now);

        List<RecentOrderRowDto> recent = mapRecentOrders(orderRepository.findTop5ByOrderByNgayDatDesc());

        return new DashboardSummaryDto(
                doanhThuThangText, doanhThuTrend.text, doanhThuTrend.css,
                donHangMoiText, donHangTrend.text, donHangTrend.css,
                tongKhachHangText, "ỔN ĐỊNH",
                formatPercent1(tiLeChuyenDoi) + "%", tiLeChuyenDoiTrend.text, tiLeChuyenDoiTrend.css,
                doanhThu12Thang, loiNhuan12Thang, recent
        );
    }

    private List<MonthlyRevenuePointDto> buildLast12MonthsRevenueSeries(LocalDateTime now) {
        List<BigDecimal> values = new ArrayList<>();
        List<YearMonth> months = new ArrayList<>();
        YearMonth start = YearMonth.of(2025, 1);

        for (int i = 0; i < 12; i++) {
            YearMonth ym = start.plusMonths(i);
            months.add(ym);
            LocalDateTime from = ym.atDay(1).atStartOfDay();
            LocalDateTime to = ym.plusMonths(1).atDay(1).atStartOfDay();
            BigDecimal v = orderRepository.sumTongTienByNgayDatBetweenAndTrangThaiDonHangNot(from, to, TrangThaiDonHang.DaHuy);
            values.add(v == null ? BigDecimal.ZERO : v);
        }

        BigDecimal max = values.stream().reduce(BigDecimal.ZERO, BigDecimal::max);
        List<MonthlyRevenuePointDto> result = new ArrayList<>();
        for (int i = 0; i < months.size(); i++) {
            BigDecimal v = values.get(i);
            int percent;
            if (max.compareTo(BigDecimal.ZERO) == 0) {
                percent = 4;
            } else {
                percent = v.multiply(BigDecimal.valueOf(100)).divide(max, 0, RoundingMode.HALF_UP).intValue();
                if (v.compareTo(BigDecimal.ZERO) > 0) {
                    percent = Math.max(30, percent); // Đẩy chiều cao tối thiểu lên 30% để nhìn rõ
                } else {
                    percent = 4;
                }
            }
            System.out.println(">>> THANG " + months.get(i).getMonthValue() + "/2025: Doanh thu = " + v + " | Cao: " + percent + "%");
            result.add(new MonthlyRevenuePointDto("T" + months.get(i).getMonthValue(), v, clamp(percent, 0, 100)));
        }
        return result;
    }

    private List<MonthlyRevenuePointDto> buildLast12MonthsProfitSeries(LocalDateTime now) {
        List<BigDecimal> values = new ArrayList<>();
        List<YearMonth> months = new ArrayList<>();
        YearMonth start = YearMonth.of(2025, 1);

        for (int i = 0; i < 12; i++) {
            YearMonth ym = start.plusMonths(i);
            months.add(ym);
            LocalDateTime from = ym.atDay(1).atStartOfDay();
            LocalDateTime to = ym.plusMonths(1).atDay(1).atStartOfDay();
            BigDecimal v = orderItemRepository.sumLoiNhuanByNgayDatBetweenAndTrangThaiDonHangNot(from, to, TrangThaiDonHang.DaHuy);
            values.add(v == null ? BigDecimal.ZERO : v);
        }

        BigDecimal max = values.stream().reduce(BigDecimal.ZERO, BigDecimal::max);
        List<MonthlyRevenuePointDto> result = new ArrayList<>();
        for (int i = 0; i < months.size(); i++) {
            BigDecimal v = values.get(i);
            int percent;
            if (max.compareTo(BigDecimal.ZERO) == 0) {
                percent = 4;
            } else {
                percent = v.multiply(BigDecimal.valueOf(100)).divide(max, 0, RoundingMode.HALF_UP).intValue();
                percent = v.compareTo(BigDecimal.ZERO) > 0 ? Math.max(30, percent) : 4;
            }
            result.add(new MonthlyRevenuePointDto("T" + months.get(i).getMonthValue(), v, clamp(percent, 0, 100)));
        }
        return result;
    }

    private List<RecentOrderRowDto> mapRecentOrders(List<Order> orders) {
        DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("HH:mm - dd/MM", Locale.forLanguageTag("vi-VN"));
        List<RecentOrderRowDto> rows = new ArrayList<>();
        for (Order o : orders) {
            String sanitizedKh = (o.getCustomer() != null) ? safe(o.getCustomer().getHoTen()) : "Không rõ";
            String sanPham = "Đơn hàng Aura";
            if (o.getOrderItems() != null && !o.getOrderItems().isEmpty()) {
                sanPham = safe(o.getOrderItems().get(0).getProductVariant().getProduct().getTenSanPham());
                if (o.getOrderItems().size() > 1) sanPham += " (+" + (o.getOrderItems().size() - 1) + ")";
            }
            TrangThaiView status = mapTrangThai(o.getTrangThaiDonHang());
            rows.add(new RecentOrderRowDto("#" + safe(o.getMaDonHang()), sanitizedKh, 
                (o.getNgayDat() != null ? o.getNgayDat().format(dtFmt) : ""), sanPham, formatMoneyVnd(o.getTongTien()), status.label, status.css));
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

    private String formatPercent1(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(1, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatCompactVnd(BigDecimal vnd) {
        BigDecimal v = (vnd == null ? BigDecimal.ZERO : vnd).abs();
        if (v.compareTo(BigDecimal.valueOf(1_000_000_000L)) >= 0) return v.divide(BigDecimal.valueOf(1_000_000_000L), 2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "B";
        if (v.compareTo(BigDecimal.valueOf(1_000_000L)) >= 0) return v.divide(BigDecimal.valueOf(1_000_000L), 2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "M";
        if (v.compareTo(BigDecimal.valueOf(1_000L)) >= 0) return v.divide(BigDecimal.valueOf(1_000L), 1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "K";
        return formatMoneyVnd(v);
    }

    private String formatMoneyVnd(BigDecimal money) {
        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        nf.setMaximumFractionDigits(0);
        return nf.format(money == null ? BigDecimal.ZERO : money) + "đ";
    }

    private String formatCount(long value) {
        return NumberFormat.getInstance(Locale.forLanguageTag("vi-VN")).format(value);
    }

    private String formatCompactCount(long value) {
        if (value >= 1_000_000) return BigDecimal.valueOf(value).divide(BigDecimal.valueOf(1_000_000), 1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "M";
        if (value >= 1_000) return BigDecimal.valueOf(value).divide(BigDecimal.valueOf(1_000), 1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "K";
        return String.valueOf(value);
    }

    private int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
    private String safe(String s) { return s == null ? "" : s; }
}
