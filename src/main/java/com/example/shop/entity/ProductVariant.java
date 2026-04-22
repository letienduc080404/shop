package com.example.shop.entity;

import com.example.shop.entity.enums.KichThuoc;
import jakarta.persistence.*;

@Entity
@Table(name = "product_variants", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ID_SanPham", "KichThuoc", "MauSac"})
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

    public ProductVariant() {}

    public Long getIdBienThe() { return idBienThe; }
    public void setIdBienThe(Long idBienThe) { this.idBienThe = idBienThe; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public KichThuoc getKichThuoc() { return kichThuoc; }
    public void setKichThuoc(KichThuoc kichThuoc) { this.kichThuoc = kichThuoc; }
    public String getMauSac() { return mauSac; }
    public void setMauSac(String mauSac) { this.mauSac = mauSac; }
    public Integer getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(Integer soLuongTon) { this.soLuongTon = soLuongTon; }
}
