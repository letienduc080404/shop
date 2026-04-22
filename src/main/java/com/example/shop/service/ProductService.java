package com.example.shop.service;

import com.example.shop.entity.Product;
import com.example.shop.entity.ProductVariant;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public ProductService(ProductRepository productRepository, 
                          ProductVariantRepository productVariantRepository) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategory_IdDanhMuc(categoryId);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }
    
    public List<ProductVariant> getVariantsByProduct(Product product) {
        return productVariantRepository.findByProduct(product);
    }
}
