package com.example.shop.dto.admin;

import java.util.List;

public class DashboardSummaryDto {
    private final String doanhThuThang;
    private final String doanhThuThangXuHuongText;
    private final String doanhThuThangXuHuongCss;

    private final String donHangMoi;
    private final String donHangMoiXuHuongText;
    private final String donHangMoiXuHuongCss;

    private final String tongKhachHang;
    private final String tongKhachHangGhiChuText;

    private final String tiLeChuyenDoi;
    private final String tiLeChuyenDoiXuHuongText;
    private final String tiLeChuyenDoiXuHuongCss;

    private final List<MonthlyRevenuePointDto> doanhThu12Thang;
    private final List<RecentOrderRowDto> giaoDichGanDay;

    public DashboardSummaryDto(
            String doanhThuThang,
            String doanhThuThangXuHuongText,
            String doanhThuThangXuHuongCss,
            String donHangMoi,
            String donHangMoiXuHuongText,
            String donHangMoiXuHuongCss,
            String tongKhachHang,
            String tongKhachHangGhiChuText,
            String tiLeChuyenDoi,
            String tiLeChuyenDoiXuHuongText,
            String tiLeChuyenDoiXuHuongCss,
            List<MonthlyRevenuePointDto> doanhThu12Thang,
            List<RecentOrderRowDto> giaoDichGanDay
    ) {
        this.doanhThuThang = doanhThuThang;
        this.doanhThuThangXuHuongText = doanhThuThangXuHuongText;
        this.doanhThuThangXuHuongCss = doanhThuThangXuHuongCss;
        this.donHangMoi = donHangMoi;
        this.donHangMoiXuHuongText = donHangMoiXuHuongText;
        this.donHangMoiXuHuongCss = donHangMoiXuHuongCss;
        this.tongKhachHang = tongKhachHang;
        this.tongKhachHangGhiChuText = tongKhachHangGhiChuText;
        this.tiLeChuyenDoi = tiLeChuyenDoi;
        this.tiLeChuyenDoiXuHuongText = tiLeChuyenDoiXuHuongText;
        this.tiLeChuyenDoiXuHuongCss = tiLeChuyenDoiXuHuongCss;
        this.doanhThu12Thang = doanhThu12Thang;
        this.giaoDichGanDay = giaoDichGanDay;
    }

    public String getDoanhThuThang() {
        return doanhThuThang;
    }

    public String getDoanhThuThangXuHuongText() {
        return doanhThuThangXuHuongText;
    }

    public String getDoanhThuThangXuHuongCss() {
        return doanhThuThangXuHuongCss;
    }

    public String getDonHangMoi() {
        return donHangMoi;
    }

    public String getDonHangMoiXuHuongText() {
        return donHangMoiXuHuongText;
    }

    public String getDonHangMoiXuHuongCss() {
        return donHangMoiXuHuongCss;
    }

    public String getTongKhachHang() {
        return tongKhachHang;
    }

    public String getTongKhachHangGhiChuText() {
        return tongKhachHangGhiChuText;
    }

    public String getTiLeChuyenDoi() {
        return tiLeChuyenDoi;
    }

    public String getTiLeChuyenDoiXuHuongText() {
        return tiLeChuyenDoiXuHuongText;
    }

    public String getTiLeChuyenDoiXuHuongCss() {
        return tiLeChuyenDoiXuHuongCss;
    }

    public List<MonthlyRevenuePointDto> getDoanhThu12Thang() {
        return doanhThu12Thang;
    }

    public List<RecentOrderRowDto> getGiaoDichGanDay() {
        return giaoDichGanDay;
    }
}

