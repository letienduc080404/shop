package com.example.shop.dto.admin;

import com.example.shop.entity.enums.HangThanhVien;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomerAdminRowDto {
    private Long idKhachHang;
    private String hoTen;
    private String email;
    private HangThanhVien hangThanhVien;

    private BigDecimal tongChiTieu = BigDecimal.ZERO;
    private long soDonHang;
    private LocalDateTime ngayDonGanNhat;

    public CustomerAdminRowDto() {}

    public CustomerAdminRowDto(Long idKhachHang, String hoTen, String email, HangThanhVien hangThanhVien) {
        this.idKhachHang = idKhachHang;
        this.hoTen = hoTen;
        this.email = email;
        this.hangThanhVien = hangThanhVien;
    }

    public Long getIdKhachHang() {
        return idKhachHang;
    }

    public void setIdKhachHang(Long idKhachHang) {
        this.idKhachHang = idKhachHang;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public HangThanhVien getHangThanhVien() {
        return hangThanhVien;
    }

    public void setHangThanhVien(HangThanhVien hangThanhVien) {
        this.hangThanhVien = hangThanhVien;
    }

    public BigDecimal getTongChiTieu() {
        return tongChiTieu;
    }

    public void setTongChiTieu(BigDecimal tongChiTieu) {
        this.tongChiTieu = tongChiTieu;
    }

    public long getSoDonHang() {
        return soDonHang;
    }

    public void setSoDonHang(long soDonHang) {
        this.soDonHang = soDonHang;
    }

    public LocalDateTime getNgayDonGanNhat() {
        return ngayDonGanNhat;
    }

    public void setNgayDonGanNhat(LocalDateTime ngayDonGanNhat) {
        this.ngayDonGanNhat = ngayDonGanNhat;
    }
}

