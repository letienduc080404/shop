package com.example.shop.entity;

import com.example.shop.entity.enums.TrangThaiSanPham;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SanPham")
    private Long idSanPham;

    @Column(name = "TenSanPham", nullable = false, length = 200)
    private String tenSanPham;

    @Column(name = "MaSKU", nullable = false, unique = true, length = 50)
    private String maSKU;

    @Column(name = "MoTa", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "GiaNiemYet", nullable = false, precision = 12, scale = 2)
    private BigDecimal giaNiemYet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DanhMuc", nullable = false)
    private Category category;

    @Column(name = "ChatLieu", length = 100)
    private String chatLieu;

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThai")
    private TrangThaiSanPham trangThai = TrangThaiSanPham.ConHang;

    @Column(name = "HinhAnh", length = 1000)
    private String hinhAnh;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ProductImage> images;

    public Product() {}

    public java.util.List<ProductImage> getImages() { return images; }
    public void setImages(java.util.List<ProductImage> images) { this.images = images; }

    public Long getIdSanPham() { return idSanPham; }
    public void setIdSanPham(Long idSanPham) { this.idSanPham = idSanPham; }
    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
    public String getMaSKU() { return maSKU; }
    public void setMaSKU(String maSKU) { this.maSKU = maSKU; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public BigDecimal getGiaNiemYet() { return giaNiemYet; }
    public void setGiaNiemYet(BigDecimal giaNiemYet) { this.giaNiemYet = giaNiemYet; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getChatLieu() { return chatLieu; }
    public void setChatLieu(String chatLieu) { this.chatLieu = chatLieu; }
    public TrangThaiSanPham getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThaiSanPham trangThai) { this.trangThai = trangThai; }
    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public int getTotalStock() {
        if (idSanPham == null) return 0;
        // Cách này hơi kém tối ưu nếu gọi nhiều lần; hiện tại admin sẽ tính tồn kho ở service
        // Tối ưu hơn là dùng truy vấn aggregate hoặc một cột lưu sẵn tổng tồn
        return 0; // Tồn kho sẽ được xử lý ở service/truy vấn tuỳ nhu cầu
    }

    public String getImageUrl() {
        if (hinhAnh != null && !hinhAnh.isEmpty()) {
            if (hinhAnh.startsWith("http") || hinhAnh.startsWith("/")) {
                return hinhAnh;
            }
            return "/images/" + hinhAnh;
        }
        if (idSanPham == null) return "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=800";
        int mod = (int) (idSanPham % 6);
        return switch (mod) {
            case 0 -> "https://images.unsplash.com/photo-1594932224440-946777db932f?w=800";
            case 1 -> "https://images.unsplash.com/photo-1539109136881-3be0616acf4b?w=800";
            case 2 -> "https://images.unsplash.com/photo-1543076447-215ad9ba6923?w=800";
            case 3 -> "https://images.unsplash.com/photo-1551488831-00ddcb6c6bd3?w=800";
            case 4 -> "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=800";
            default -> "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800";
        };
    }
}
