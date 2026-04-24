package com.example.shop.service;

import com.example.shop.entity.Product;
import com.example.shop.entity.ProductVariant;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.ProductVariantRepository;
import com.example.shop.repository.CategoryRepository;
import com.example.shop.repository.ProductImageRepository;
import com.example.shop.repository.OrderItemRepository;
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
    private final OrderItemRepository orderItemRepository;
    private final FileStorageService fileStorageService;

    public ProductService(ProductRepository productRepository, 
                          ProductVariantRepository productVariantRepository,
                          CategoryRepository categoryRepository,
                          ProductImageRepository productImageRepository,
                          OrderItemRepository orderItemRepository,
                          FileStorageService fileStorageService) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.orderItemRepository = orderItemRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> searchProducts(String keyword, Long categoryId) {
        if (keyword != null && !keyword.isEmpty()) {
            if (categoryId != null) {
                return productRepository.findByTenSanPhamContainingIgnoreCaseAndCategory_IdDanhMuc(keyword, categoryId);
            } else {
                return productRepository.findByTenSanPhamContainingIgnoreCase(keyword);
            }
        } else if (categoryId != null) {
            return productRepository.findByCategory_IdDanhMuc(categoryId);
        } else {
            return productRepository.findAll();
        }
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

    public ProductVariant getVariantById(Long id) {
        return productVariantRepository.findById(id).orElse(null);
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
            // Kiểm tra xem sản phẩm đã có trong đơn hàng nào chưa
            if (orderItemRepository.existsByProductVariant_Product_IdSanPham(id)) {
                throw new RuntimeException("Không thể xóa sản phẩm này vì nó đã tồn tại trong các đơn hàng. Vui lòng cập nhật trạng thái ngừng kinh doanh thay vì xóa.");
            }

            // 1. Xoá ảnh đại diện chính khỏi ổ đĩa
            fileStorageService.deleteFile(product.getHinhAnh());
            
            // 2. Xoá các ảnh bổ sung khỏi ổ đĩa (nếu có)
            if (product.getImages() != null) {
                product.getImages().forEach(img -> fileStorageService.deleteFile(img.getDuongDan()));
            }

            // 3. Xoá các biến thể liên quan trong CSDL
            productVariantRepository.deleteByProduct(product);
            
            // 4. Xoá sản phẩm trong CSDL (bản ghi ảnh sẽ bị xoá theo CascadeType.ALL)
            productRepository.delete(product);
        }
    }

    @Transactional
    public Product createProduct(String tenSanPham, Long idDanhMuc, BigDecimal giaNiemYet, String moTa, 
                                 String mauSacs, List<String> sizes, Integer soLuong, MultipartFile[] files) {
        
        Product product = new Product();
        product.setTenSanPham(tenSanPham);
        product.setGiaNiemYet(giaNiemYet);
        product.setMoTa(moTa);
        product.setMaSKU("SKU-" + System.currentTimeMillis() % 1000000);
        
        Category category = categoryRepository.findById(idDanhMuc).orElse(null);
        product.setCategory(category);
        
        Product savedProduct = productRepository.save(product);
        
        // Xử lý ảnh
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
        
        // Xử lý biến thể (màu + size)
        String[] colorList = mauSacs.split(",");
        for (String color : colorList) {
            String trimmedColor = color.trim();
            if (trimmedColor.isEmpty()) continue;
            
            for (String sizeStr : sizes) {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(savedProduct);
                variant.setMauSac(trimmedColor);
                variant.setKichThuoc(KichThuoc.valueOf(sizeStr));
                variant.setSoLuongTon(soLuong != null ? soLuong : 0);
                productVariantRepository.save(variant);
            }
        }
        
        return savedProduct;
    }

    @Transactional
    public void updateProduct(Long id, String tenSanPham, Long idDanhMuc, BigDecimal giaNiemYet, String moTa, MultipartFile[] files) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        
        product.setTenSanPham(tenSanPham);
        product.setGiaNiemYet(giaNiemYet);
        product.setMoTa(moTa);
        
        Category category = categoryRepository.findById(idDanhMuc).orElse(null);
        product.setCategory(category);
        
        // Nếu có tải lên ảnh mới
        if (files != null && files.length > 0 && !files[0].isEmpty()) {
            // Xoá ảnh cũ (về mặt vật lý và DB)
            if (product.getImages() != null) {
                // Không xóa toàn bộ ảnh cũ trong DB nếu Cascade chưa chuẩn, ta sẽ xóa rõ ràng
                productImageRepository.deleteByProduct(product);
                product.getImages().clear();
            }
            
            // Lưu ảnh mới
            for (int i = 0; i < files.length; i++) {
                if (files[i].isEmpty()) continue;
                String path = fileStorageService.storeFile(files[i]);
                ProductImage productImage = new ProductImage(path, product);
                productImageRepository.save(productImage);
                if (i == 0) product.setHinhAnh(path);
            }
        }
        
        productRepository.save(product);
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
