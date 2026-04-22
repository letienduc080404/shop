package com.example.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DanhMuc")
    private Long idDanhMuc;

    @Column(name = "TenDanhMuc", nullable = false, unique = true, length = 100)
    private String tenDanhMuc;

    public Category() {
    }

    public Category(String tenDanhMuc) {
        this.tenDanhMuc = tenDanhMuc;
    }

    public Long getIdDanhMuc() {
        return idDanhMuc;
    }

    public void setIdDanhMuc(Long idDanhMuc) {
        this.idDanhMuc = idDanhMuc;
    }

    public String getTenDanhMuc() {
        return tenDanhMuc;
    }

    public void setTenDanhMuc(String tenDanhMuc) {
        this.tenDanhMuc = tenDanhMuc;
    }
}
