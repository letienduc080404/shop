package com.example.shop.service;

import com.example.shop.entity.Product;
import com.example.shop.entity.ProductVariant;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.ProductVariantRepository;
import com.example.shop.repository.CategoryRepository;
import com.example.shop.repository.ProductImageRepository;
import com.example.shop.entity.Category;
import com.example.shop.entity.ProductImage;
import com.example.shop.entity.enums.KichThuoc;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final FileStorageService fileStorageService;

    public ProductService(ProductRepository productRepository, 
                          ProductVariantRepository productVariantRepository,
                          CategoryRepository categoryRepository,
                          ProductImageRepository productImageRepository,
                          FileStorageService fileStorageService) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
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

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void saveVariant(ProductVariant variant) {
        productVariantRepository.save(variant);
    }

    @Transactional
    public void deleteProductById(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            // 1. Delete main thumbnail image from disk
            fileStorageService.deleteFile(product.getHinhAnh());
            
            // 2. Delete additional pictures from disk (if any)
            if (product.getImages() != null) {
                product.getImages().forEach(img -> fileStorageService.deleteFile(img.getDuongDan()));
            }

            // 3. Delete associated variants from DB
            productVariantRepository.deleteByProduct(product);
            
            // 4. Delete product from DB (images records in DB will be deleted via CascadeType.ALL)
            productRepository.delete(product);
        }
    }

    @Transactional
    public Product createProduct(String tenSanPham, Long idDanhMuc, BigDecimal giaNiemYet, String moTa, 
                                String mauSacs, List<String> sizes, MultipartFile[] files) {
        
        Product product = new Product();
        product.setTenSanPham(tenSanPham);
        product.setGiaNiemYet(giaNiemYet);
        product.setMoTa(moTa);
        product.setMaSKU("SKU-" + System.currentTimeMillis() % 1000000);
        
        Category category = categoryRepository.findById(idDanhMuc).orElse(null);
        product.setCategory(category);
        
        Product savedProduct = productRepository.save(product);
        
        // Handle images
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isEmpty()) continue;
                String path = fileStorageService.storeFile(files[i]);
                
                ProductImage productImage = new ProductImage(path, savedProduct);
                productImageRepository.save(productImage);
                
                if (i == 0) {
                    savedProduct.setHinhAnh(path);
                    productRepository.save(savedProduct);
                }
            }
        }
        
        // Handle Variants
        String[] colorList = mauSacs.split(",");
        for (String color : colorList) {
            String trimmedColor = color.trim();
            if (trimmedColor.isEmpty()) continue;
            
            for (String sizeStr : sizes) {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(savedProduct);
                variant.setMauSac(trimmedColor);
                variant.setKichThuoc(KichThuoc.valueOf(sizeStr));
                variant.setSoLuongTon(100); // Default stock
                productVariantRepository.save(variant);
            }
        }
        
        return savedProduct;
    }

    public Map<Long, Integer> getProductStockMap(List<Product> products) {
        Map<Long, Integer> productStocks = new HashMap<>();
        for (Product product : products) {
            List<ProductVariant> variants = productVariantRepository.findByProduct(product);
            int totalStock = variants.stream()
                    .mapToInt(ProductVariant::getSoLuongTon)
                    .sum();
            productStocks.put(product.getIdSanPham(), totalStock);
        }
        return productStocks;
    }
}
