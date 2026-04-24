package com.example.shop.dto.admin;

public class TopProductAnalyticsRowDto {
    private final String tenSanPham;
    private final String danhMuc;
    private final String imageUrl;
    private final String giaNiemYet;
    private final long daBan;
    private final String doanhThu;
    private final String hieuSuatLabel;
    private final String hieuSuatCss;

    public TopProductAnalyticsRowDto(
            String tenSanPham,
            String danhMuc,
            String imageUrl,
            String giaNiemYet,
            long daBan,
            String doanhThu,
            String hieuSuatLabel,
            String hieuSuatCss
    ) {
        this.tenSanPham = tenSanPham;
        this.danhMuc = danhMuc;
        this.imageUrl = imageUrl;
        this.giaNiemYet = giaNiemYet;
        this.daBan = daBan;
        this.doanhThu = doanhThu;
        this.hieuSuatLabel = hieuSuatLabel;
        this.hieuSuatCss = hieuSuatCss;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public String getDanhMuc() {
        return danhMuc;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getGiaNiemYet() {
        return giaNiemYet;
    }

    public long getDaBan() {
        return daBan;
    }

    public String getDoanhThu() {
        return doanhThu;
    }

    public String getHieuSuatLabel() {
        return hieuSuatLabel;
    }

    public String getHieuSuatCss() {
        return hieuSuatCss;
    }
}

