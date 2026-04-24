package com.example.shop.service;

import com.example.shop.entity.Category;
import com.example.shop.repository.CategoryRepository;
import com.example.shop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public boolean existsByTenDanhMuc(String tenDanhMuc) {
        return categoryRepository.existsByTenDanhMuc(tenDanhMuc);
    }

    @Transactional
    public void deleteCategory(Long id) {
        // Kiểm tra xem có sản phẩm nào thuộc danh mục này không
        if (!productRepository.findByCategory_IdDanhMuc(id).isEmpty()) {
            throw new RuntimeException("Không thể xóa danh mục này vì vẫn còn sản phẩm thuộc danh mục. Vui lòng chuyển hoặc xóa các sản phẩm đó trước.");
        }
        categoryRepository.deleteById(id);
    }
}
