package com.example.shop.repository;

import com.example.shop.entity.Product;
import com.example.shop.entity.ProductVariant;
import com.example.shop.entity.enums.KichThuoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProduct(Product product);
    List<ProductVariant> findByProduct_IdSanPhamAndKichThuoc(Long idSanPham, KichThuoc kichThuoc);
    void deleteByProduct(Product product);
}
