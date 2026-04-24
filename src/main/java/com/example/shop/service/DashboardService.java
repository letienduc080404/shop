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

    /**
     * Tính toàn bộ số liệu cho trang dashboard admin.
     * Thiết kế theo hướng OOP: Controller gọi 1 service duy nhất, service tự tổng hợp dữ liệu từ repository.
     */
    public DashboardSummaryDto buildAdminDashboardSummary() {
        LocalDateTime now = LocalDateTime.now();

        YearMonth currentMonth = YearMonth.from(now);
        YearMonth previousMonth = currentMonth.minusMonths(1);

        LocalDateTime startCurrentMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime startNextMonth = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        LocalDateTime startPreviousMonth = previousMonth.atDay(1).atStartOfDay();
        LocalDateTime startCurrentMonthFromPrev = currentMonth.atDay(1).atStartOfDay();

        // Doanh thu: tính theo đơn Hoàn thành
        BigDecimal doanhThuThang = orderRepository.sumTongTienByNgayDatBetweenAndTrangThai(
                startCurrentMonth,
                startNextMonth,
                TrangThaiDonHang.HoanThanh
        );
        BigDecimal doanhThuThangTruoc = orderRepository.sumTongTienByNgayDatBetweenAndTrangThai(
                startPreviousMonth,
                startCurrentMonthFromPrev,
                TrangThaiDonHang.HoanThanh
        );

        String doanhThuThangText = formatCompactVnd(doanhThuThang);
        TrendView doanhThuTrend = calcTrendPercent(doanhThuThang, doanhThuThangTruoc);

        // Đơn hàng mới: đếm theo thời gian, loại trừ đơn bị huỷ
        long donHangMoi = orderRepository.countByNgayDatBetweenAndTrangThaiDonHangNot(
                startCurrentMonth,
                startNextMonth,
                TrangThaiDonHang.DaHuy
        );
        long donHangMoiThangTruoc = orderRepository.countByNgayDatBetweenAndTrangThaiDonHangNot(
                startPreviousMonth,
                startCurrentMonthFromPrev,
                TrangThaiDonHang.DaHuy
        );
        TrendView donHangTrend = calcTrendPercent(BigDecimal.valueOf(donHangMoi), BigDecimal.valueOf(donHangMoiThangTruoc));

        String donHangMoiText = formatCount(donHangMoi);

        // Khách hàng: tổng số tài khoản khách (đang để đơn giản: tổng bản ghi customers)
        long tongKhachHang = customerRepository.count();
        String tongKhachHangText = formatCompactCount(tongKhachHang);

        // Tỉ lệ chuyển đổi (đơn giản): số khách có đặt đơn trong 30 ngày / tổng khách
        LocalDateTime start30Days = now.minusDays(30);
        LocalDateTime start60Days = now.minusDays(60);

        long khachCoDon30Ngay = orderRepository.countDistinctCustomersByNgayDatBetween(start30Days, now);
        long khachCoDon30NgayTruoc = orderRepository.countDistinctCustomersByNgayDatBetween(start60Days, start30Days);

        BigDecimal tiLeChuyenDoi = calcRatePercent(khachCoDon30Ngay, tongKhachHang);
        BigDecimal tiLeChuyenDoiTruoc = calcRatePercent(khachCoDon30NgayTruoc, tongKhachHang);
        TrendView tiLeChuyenDoiTrend = calcTrendPercent(tiLeChuyenDoi, tiLeChuyenDoiTruoc);

        // 7 ngày gần nhất (dễ quan sát xu hướng tuần)
        List<MonthlyRevenuePointDto> doanhThu12Thang = buildLast7DaysRevenueSeries(now);
        List<MonthlyRevenuePointDto> loiNhuan12Thang = buildLast7DaysProfitSeries(now);

        // Giao dịch gần đây
        List<RecentOrderRowDto> recent = mapRecentOrders(orderRepository.findTop5ByOrderByNgayDatDesc());

        return new DashboardSummaryDto(
                doanhThuThangText,
                doanhThuTrend.text,
                doanhThuTrend.css,
                donHangMoiText,
                donHangTrend.text,
                donHangTrend.css,
                tongKhachHangText,
                "ỔN ĐỊNH",
                formatPercent1(tiLeChuyenDoi) + "%",
                tiLeChuyenDoiTrend.text,
                tiLeChuyenDoiTrend.css,
                doanhThu12Thang,
                loiNhuan12Thang,
                recent
        );
    }

    private List<MonthlyRevenuePointDto> buildLast7DaysRevenueSeries(LocalDateTime now) {
        List<BigDecimal> values = new ArrayList<>();
        List<LocalDateTime> starts = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime from = now.minusDays(i).toLocalDate().atStartOfDay();
            LocalDateTime to = from.plusDays(1);
            starts.add(from);
            BigDecimal v = orderRepository.sumTongTienByNgayDatBetweenAndTrangThai(from, to, TrangThaiDonHang.HoanThanh);
            values.add(v == null ? BigDecimal.ZERO : v);
        }

        BigDecimal max = values.stream().reduce(BigDecimal.ZERO, (a, b) -> a.max(b));

        List<MonthlyRevenuePointDto> result = new ArrayList<>();
        for (int i = 0; i < starts.size(); i++) {
            String label = dayLabel(starts.get(i).getDayOfWeek());
            BigDecimal v = values.get(i);
            int percent;
            if (max.compareTo(BigDecimal.ZERO) == 0) {
                percent = 12;
            } else {
                percent = v
                        .multiply(BigDecimal.valueOf(100))
                        .divide(max, 0, RoundingMode.HALF_UP)
                        .intValue();
                percent = Math.max(4, percent);
            }
            result.add(new MonthlyRevenuePointDto(label, v, clamp(percent, 0, 100)));
        }
        return result;
    }

    private List<MonthlyRevenuePointDto> buildLast7DaysProfitSeries(LocalDateTime now) {
        List<BigDecimal> values = new ArrayList<>();
        List<LocalDateTime> starts = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime from = now.minusDays(i).toLocalDate().atStartOfDay();
            LocalDateTime to = from.plusDays(1);
            starts.add(from);
            BigDecimal v = orderItemRepository.sumLoiNhuanByNgayDatBetweenAndTrangThai(from, to, TrangThaiDonHang.HoanThanh);
            values.add(v == null ? BigDecimal.ZERO : v);
        }

        BigDecimal max = values.stream().reduce(BigDecimal.ZERO, (a, b) -> a.max(b));

        List<MonthlyRevenuePointDto> result = new ArrayList<>();
        for (int i = 0; i < starts.size(); i++) {
            String label = dayLabel(starts.get(i).getDayOfWeek());
            BigDecimal v = values.get(i);
            int percent;
            if (max.compareTo(BigDecimal.ZERO) == 0) {
                percent = 12;
            } else {
                percent = v
                        .multiply(BigDecimal.valueOf(100))
                        .divide(max, 0, RoundingMode.HALF_UP)
                        .intValue();
                percent = Math.max(4, percent);
            }
            result.add(new MonthlyRevenuePointDto(label, v, clamp(percent, 0, 100)));
        }
        return result;
    }

    private String dayLabel(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "T2";
            case TUESDAY -> "T3";
            case WEDNESDAY -> "T4";
            case THURSDAY -> "T5";
            case FRIDAY -> "T6";
            case SATURDAY -> "T7";
            case SUNDAY -> "CN";
        };
    }

    private List<RecentOrderRowDto> mapRecentOrders(List<Order> orders) {
        DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("HH:mm - dd/MM", Locale.forLanguageTag("vi-VN"));
        List<RecentOrderRowDto> rows = new ArrayList<>();

        for (Order o : orders) {
            String ma = safe(o.getMaDonHang());
            String kh = (o.getCustomer() != null) ? safe(o.getCustomer().getHoTen()) : "Không rõ";
            String ngayGio = (o.getNgayDat() != null) ? o.getNgayDat().format(dtFmt) : "";

            String sanPham = "Nhiều sản phẩm";
            List<OrderItem> items = o.getOrderItems();
            if (items != null && !items.isEmpty() && items.get(0).getProductVariant() != null
                    && items.get(0).getProductVariant().getProduct() != null) {
                sanPham = safe(items.get(0).getProductVariant().getProduct().getTenSanPham());
                if (items.size() > 1) {
                    sanPham = sanPham + " (+" + (items.size() - 1) + ")";
                }
            }

            String giaTri = formatMoneyVnd(o.getTongTien());

            TrangThaiView status = mapTrangThai(o.getTrangThaiDonHang());
            rows.add(new RecentOrderRowDto(
                    "#" + ma,
                    kh,
                    ngayGio,
                    sanPham,
                    giaTri,
                    status.label,
                    status.css
            ));
        }
        return rows;
    }

    private static class TrangThaiView {
        private final String label;
        private final String css;

        private TrangThaiView(String label, String css) {
            this.label = label;
            this.css = css;
        }
    }

    private TrangThaiView mapTrangThai(TrangThaiDonHang status) {
        if (status == null) {
            return new TrangThaiView("Không rõ", "bg-gray-100 text-gray-700");
        }
        return switch (status) {
            case HoanThanh -> new TrangThaiView("Hoàn thành", "bg-green-100 text-green-700");
            case DangGiao -> new TrangThaiView("Đang giao", "bg-yellow-100 text-yellow-700");
            case ChoXuLy -> new TrangThaiView("Đang xử lý", "bg-yellow-100 text-yellow-700");
            case DaHuy -> new TrangThaiView("Đã huỷ", "bg-red-100 text-secondary");
        };
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
     * - Nếu kỳ trước = 0: hiển thị “+0.0%” (tránh chia 0).
     */
    private TrendView calcTrendPercent(BigDecimal current, BigDecimal previous) {
        BigDecimal cur = current == null ? BigDecimal.ZERO : current;
        BigDecimal prev = previous == null ? BigDecimal.ZERO : previous;

        if (prev.compareTo(BigDecimal.ZERO) == 0) {
            if (cur.compareTo(BigDecimal.ZERO) == 0) {
                return new TrendView("+0.0%", "text-primary/40");
            }
            return new TrendView("+100.0%", "text-green-600");
        }

        BigDecimal delta = cur.subtract(prev)
                .multiply(BigDecimal.valueOf(100))
                .divide(prev, 1, RoundingMode.HALF_UP);

        String sign = delta.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        String text = sign + delta.toPlainString() + "%";
        String css = delta.compareTo(BigDecimal.ZERO) >= 0 ? "text-green-600" : "text-secondary";
        return new TrendView(text, css);
    }

    private BigDecimal calcRatePercent(long numerator, long denominator) {
        if (denominator <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private String formatPercent1(BigDecimal value) {
        BigDecimal v = value == null ? BigDecimal.ZERO : value;
        return v.setScale(1, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * Rút gọn tiền VND theo K/M/B để đúng “2.48B” như UI mẫu.
     */
    private String formatCompactVnd(BigDecimal vnd) {
        BigDecimal v = vnd == null ? BigDecimal.ZERO : vnd;
        BigDecimal abs = v.abs();

        BigDecimal oneB = BigDecimal.valueOf(1_000_000_000L);
        BigDecimal oneM = BigDecimal.valueOf(1_000_000L);
        BigDecimal oneK = BigDecimal.valueOf(1_000L);

        if (abs.compareTo(oneB) >= 0) {
            return abs.divide(oneB, 2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "B";
        }
        if (abs.compareTo(oneM) >= 0) {
            return abs.divide(oneM, 2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "M";
        }
        if (abs.compareTo(oneK) >= 0) {
            return abs.divide(oneK, 1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "K";
        }
        return formatMoneyVnd(abs);
    }

    private String formatMoneyVnd(BigDecimal money) {
        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        nf.setMaximumFractionDigits(0);
        BigDecimal v = money == null ? BigDecimal.ZERO : money;
        return nf.format(v) + "đ";
    }

    private String formatCount(long value) {
        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        nf.setMaximumFractionDigits(0);
        return nf.format(value);
    }

    private String formatCompactCount(long value) {
        if (value >= 1_000_000) {
            return BigDecimal.valueOf(value).divide(BigDecimal.valueOf(1_000_000), 1, RoundingMode.HALF_UP)
                    .stripTrailingZeros().toPlainString() + "M";
        }
        if (value >= 1_000) {
            return BigDecimal.valueOf(value).divide(BigDecimal.valueOf(1_000), 1, RoundingMode.HALF_UP)
                    .stripTrailingZeros().toPlainString() + "K";
        }
        return String.valueOf(value);
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}

