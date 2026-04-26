package com.example.shop.entity;

import com.example.shop.entity.enums.KichThuoc;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "ID_SanPham", "KichThuoc", "MauSac" })
})
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_BienThe")
    private Long idBienThe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SanPham", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "KichThuoc", nullable = false)
    private KichThuoc kichThuoc;

    @Column(name = "MauSac", nullable = false, length = 50)
    private String mauSac;

    @Column(name = "SoLuongTon", nullable = false)
    private Integer soLuongTon = 0;

    /**
     * Giá vốn (chi phí nhập/giá gốc) của 1 sản phẩm theo biến thể.
     * Dùng để tính "Lợi nhuận" = (Giá bán - Giá vốn) * Số lượng.
     * Nếu chưa nhập giá vốn, hệ thống coi như 0 để tránh lỗi.
     */
    @Column(name = "GiaVon", precision = 12, scale = 2)
    private BigDecimal giaVon = BigDecimal.ZERO;

    public ProductVariant() {
    }

    public Long getIdBienThe() {
        return idBienThe;
    }

    public void setIdBienThe(Long idBienThe) {
        this.idBienThe = idBienThe;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public KichThuoc getKichThuoc() {
        return kichThuoc;
    }

    public void setKichThuoc(KichThuoc kichThuoc) {
        this.kichThuoc = kichThuoc;
    }

    public String getMauSac() {
        return mauSac;
    }

    public void setMauSac(String mauSac) {
        this.mauSac = mauSac;
    }

    public Integer getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(Integer soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    public BigDecimal getGiaVon() {
        return giaVon;
    }

    public void setGiaVon(BigDecimal giaVon) {
        this.giaVon = giaVon;
    }
}
