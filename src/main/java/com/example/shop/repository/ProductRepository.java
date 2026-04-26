package com.example.shop.repository;

import com.example.shop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory_IdDanhMuc(Long categoryId);

    List<Product> findByTenSanPhamContainingIgnoreCase(String keyword);

    List<Product> findByTenSanPhamContainingIgnoreCaseAndCategory_IdDanhMuc(String keyword, Long categoryId);

    // Lấy nhanh vài sản phẩm mới nhất (dùng làm fallback khi kỳ báo cáo chưa có đơn hàng)
    List<Product> findTop3ByOrderByIdSanPhamDesc();

    List<Product> findTop8ByOrderByIdSanPhamDesc();
}
