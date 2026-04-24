package com.example.shop.dto.admin;

public class RecentOrderRowDto {
    private final String maDon;
    private final String khachHang;
    private final String ngayGio;
    private final String sanPham;
    private final String giaTri;
    private final String trangThaiLabel;
    private final String trangThaiCss;

    public RecentOrderRowDto(
            String maDon,
            String khachHang,
            String ngayGio,
            String sanPham,
            String giaTri,
            String trangThaiLabel,
            String trangThaiCss
    ) {
        this.maDon = maDon;
        this.khachHang = khachHang;
        this.ngayGio = ngayGio;
        this.sanPham = sanPham;
        this.giaTri = giaTri;
        this.trangThaiLabel = trangThaiLabel;
        this.trangThaiCss = trangThaiCss;
    }

    public String getMaDon() {
        return maDon;
    }

    public String getKhachHang() {
        return khachHang;
    }

    public String getNgayGio() {
        return ngayGio;
    }

    public String getSanPham() {
        return sanPham;
    }

    public String getGiaTri() {
        return giaTri;
    }

    public String getTrangThaiLabel() {
        return trangThaiLabel;
    }

    public String getTrangThaiCss() {
        return trangThaiCss;
    }
}

