package com.example.shop.dto.admin;

import java.util.List;

public class AnalyticsSummaryDto {
    private final String kyBaoCaoLabel;

    private final String tongDoanhThu;
    private final String tongDoanhThuTrendText;
    private final String tongDoanhThuTrendCss;

    private final String tiLeChuyenDoi;
    private final String tiLeChuyenDoiTrendText;
    private final String tiLeChuyenDoiTrendCss;

    private final String donHangMoi;
    private final String tongGiaoDich;

    private final String aov;

    private final String svgPathThangNay;
    private final String svgPathThangTruoc;
    private final String tooltipText;
    private final String tooltipLeftCss;

    private final List<TopProductAnalyticsRowDto> topProducts;

    public AnalyticsSummaryDto(
            String kyBaoCaoLabel,
            String tongDoanhThu,
            String tongDoanhThuTrendText,
            String tongDoanhThuTrendCss,
            String tiLeChuyenDoi,
            String tiLeChuyenDoiTrendText,
            String tiLeChuyenDoiTrendCss,
            String donHangMoi,
            String tongGiaoDich,
            String aov,
            String svgPathThangNay,
            String svgPathThangTruoc,
            String tooltipText,
            String tooltipLeftCss,
            List<TopProductAnalyticsRowDto> topProducts
    ) {
        this.kyBaoCaoLabel = kyBaoCaoLabel;
        this.tongDoanhThu = tongDoanhThu;
        this.tongDoanhThuTrendText = tongDoanhThuTrendText;
        this.tongDoanhThuTrendCss = tongDoanhThuTrendCss;
        this.tiLeChuyenDoi = tiLeChuyenDoi;
        this.tiLeChuyenDoiTrendText = tiLeChuyenDoiTrendText;
        this.tiLeChuyenDoiTrendCss = tiLeChuyenDoiTrendCss;
        this.donHangMoi = donHangMoi;
        this.tongGiaoDich = tongGiaoDich;
        this.aov = aov;
        this.svgPathThangNay = svgPathThangNay;
        this.svgPathThangTruoc = svgPathThangTruoc;
        this.tooltipText = tooltipText;
        this.tooltipLeftCss = tooltipLeftCss;
        this.topProducts = topProducts;
    }

    public String getKyBaoCaoLabel() {
        return kyBaoCaoLabel;
    }

    public String getTongDoanhThu() {
        return tongDoanhThu;
    }

    public String getTongDoanhThuTrendText() {
        return tongDoanhThuTrendText;
    }

    public String getTongDoanhThuTrendCss() {
        return tongDoanhThuTrendCss;
    }

    public String getTiLeChuyenDoi() {
        return tiLeChuyenDoi;
    }

    public String getTiLeChuyenDoiTrendText() {
        return tiLeChuyenDoiTrendText;
    }

    public String getTiLeChuyenDoiTrendCss() {
        return tiLeChuyenDoiTrendCss;
    }

    public String getDonHangMoi() {
        return donHangMoi;
    }

    public String getTongGiaoDich() {
        return tongGiaoDich;
    }

    public String getAov() {
        return aov;
    }

    public String getSvgPathThangNay() {
        return svgPathThangNay;
    }

    public String getSvgPathThangTruoc() {
        return svgPathThangTruoc;
    }

    public String getTooltipText() {
        return tooltipText;
    }

    public String getTooltipLeftCss() {
        return tooltipLeftCss;
    }

    public List<TopProductAnalyticsRowDto> getTopProducts() {
        return topProducts;
    }
}

