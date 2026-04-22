package com.example.shop.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ChiTiet")
    private Long idChiTiet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DonHang", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_BienThe", nullable = false)
    private ProductVariant productVariant;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Column(name = "GiaBan", nullable = false, precision = 12, scale = 2)
    private BigDecimal giaBan;

    public OrderItem() {}

    public Long getIdChiTiet() { return idChiTiet; }
    public void setIdChiTiet(Long idChiTiet) { this.idChiTiet = idChiTiet; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public ProductVariant getProductVariant() { return productVariant; }
    public void setProductVariant(ProductVariant productVariant) { this.productVariant = productVariant; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    public BigDecimal getGiaBan() { return giaBan; }
    public void setGiaBan(BigDecimal giaBan) { this.giaBan = giaBan; }
}
