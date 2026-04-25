package com.example.shop.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_images")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Anh")
    private Long idAnh;

    @Column(name = "DuongDan", nullable = false, length = 1000)
    private String duongDan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SanPham", nullable = false)
    private Product product;

    public ProductImage() {}

    public ProductImage(String duongDan, Product product) {
        this.duongDan = duongDan;
        this.product = product;
    }

    public Long getIdAnh() { return idAnh; }
    public void setIdAnh(Long idAnh) { this.idAnh = idAnh; }
    public String getDuongDan() { return duongDan; }
    public void setDuongDan(String duongDan) { this.duongDan = duongDan; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getImageUrl() {
        if (duongDan != null && !duongDan.isEmpty()) {
            // Path bắt đầu bằng / hoặc http - trả về trực tiếp
            if (duongDan.startsWith("http") || duongDan.startsWith("/")) {
                return duongDan;
            }
            // Drive ID (không có dấu chấm, không có extension)
            if (!duongDan.contains(".")) {
                return "https://lh3.googleusercontent.com/d/" + duongDan;
            }
            // Tên file cũ
            return "/uploads/" + duongDan;
        }
        return "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=800";
    }
}
