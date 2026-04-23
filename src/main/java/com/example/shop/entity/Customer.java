package com.example.shop.entity;

import com.example.shop.entity.enums.HangThanhVien;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_KhachHang")
    private Long idKhachHang;

    @Column(name = "HoTen", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "Email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "MatKhau", nullable = false, length = 255)
    private String matKhau;

    @Column(name = "SoDienThoai", length = 20)
    private String soDienThoai;

    @Column(name = "DiaChi", length = 255)
    private String diaChi;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "HangThanhVien")
    private HangThanhVien hangThanhVien = HangThanhVien.Dong;

    @Column(name = "Role", length = 50)
    private String role = "ROLE_USER";

    public Customer() {}

    public Long getIdKhachHang() { return idKhachHang; }
    public void setIdKhachHang(Long idKhachHang) { this.idKhachHang = idKhachHang; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
    public HangThanhVien getHangThanhVien() { return hangThanhVien; }
    public void setHangThanhVien(HangThanhVien hangThanhVien) { this.hangThanhVien = hangThanhVien; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
