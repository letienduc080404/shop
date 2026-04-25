package com.example.shop.repository;

import com.example.shop.entity.Product;
import com.example.shop.entity.ProductVariant;
import com.example.shop.entity.enums.KichThuoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProduct(Product product);
    List<ProductVariant> findByProduct_IdSanPhamAndKichThuoc(Long idSanPham, KichThuoc kichThuoc);
    void deleteByProduct(Product product);

    interface ProductStockView {
        Long getProductId();
        Integer getTotalStock();
    }

    @Query("SELECT pv.product.idSanPham as productId, cast(SUM(pv.soLuongTon) as int) as totalStock FROM ProductVariant pv WHERE pv.product.idSanPham IN :productIds GROUP BY pv.product.idSanPham")
    List<ProductStockView> sumStockByProductIds(@Param("productIds") List<Long> productIds);
}
