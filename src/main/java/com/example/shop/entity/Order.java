package com.example.shop.entity;

import com.example.shop.entity.enums.PhuongThucThanhToan;
import com.example.shop.entity.enums.TrangThaiDonHang;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DonHang")
    private Long idDonHang;

    @Column(name = "MaDonHang", nullable = false, unique = true, length = 50)
    private String maDonHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_KhachHang", nullable = false)
    private Customer customer;

    @Column(name = "NgayDat")
    private LocalDateTime ngayDat = LocalDateTime.now();

    @Column(name = "TongTien", nullable = false, precision = 12, scale = 2)
    private BigDecimal tongTien = BigDecimal.ZERO;

    @Column(name = "TienGiamGia", precision = 12, scale = 2)
    private BigDecimal tienGiamGia = BigDecimal.ZERO;

    @Column(name = "MaUuDai", length = 50)
    private String maUuDai;

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThaiDonHang")
    private TrangThaiDonHang trangThaiDonHang = TrangThaiDonHang.ChoXuLy;

    @Enumerated(EnumType.STRING)
    @Column(name = "PhuongThucThanhToan", nullable = false)
    private PhuongThucThanhToan phuongThucThanhToan;

    @Column(name = "DiaChiNH", length = 255)
    private String diaChiNH;

    @Column(name = "SoDienThoaiNH", length = 20)
    private String soDienThoaiNH;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<OrderItem> orderItems;

    public Order() {}

    public Long getIdDonHang() { return idDonHang; }
    public void setIdDonHang(Long idDonHang) { this.idDonHang = idDonHang; }
    public String getMaDonHang() { return maDonHang; }
    public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public java.time.LocalDateTime getNgayDat() { return ngayDat; }
    public void setNgayDat(java.time.LocalDateTime ngayDat) { this.ngayDat = ngayDat; }
    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }
    public BigDecimal getTienGiamGia() { return tienGiamGia; }
    public void setTienGiamGia(BigDecimal tienGiamGia) { this.tienGiamGia = tienGiamGia; }
    public String getMaUuDai() { return maUuDai; }
    public void setMaUuDai(String maUuDai) { this.maUuDai = maUuDai; }
    public TrangThaiDonHang getTrangThaiDonHang() { return trangThaiDonHang; }
    public void setTrangThaiDonHang(TrangThaiDonHang trangThaiDonHang) { this.trangThaiDonHang = trangThaiDonHang; }
    public PhuongThucThanhToan getPhuongThucThanhToan() { return phuongThucThanhToan; }
    public void setPhuongThucThanhToan(PhuongThucThanhToan phuongThucThanhToan) { this.phuongThucThanhToan = phuongThucThanhToan; }
    public String getDiaChiNH() { return diaChiNH; }
    public void setDiaChiNH(String diaChiNH) { this.diaChiNH = diaChiNH; }
    public String getSoDienThoaiNH() { return soDienThoaiNH; }
    public void setSoDienThoaiNH(String soDienThoaiNH) { this.soDienThoaiNH = soDienThoaiNH; }
    public java.util.List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(java.util.List<OrderItem> orderItems) { this.orderItems = orderItems; }

    @Column(name = "GhiChu", length = 500)
    private String ghiChu;

    public String getGhiChu() { return ghiChu != null ? java.text.Normalizer.normalize(ghiChu, java.text.Normalizer.Form.NFC) : null; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
