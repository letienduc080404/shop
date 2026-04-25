package com.example.shop;

import com.example.shop.entity.Category;
import com.example.shop.entity.Customer;
import com.example.shop.entity.Product;
import com.example.shop.entity.ProductVariant;
import com.example.shop.entity.enums.KichThuoc;
import com.example.shop.repository.CategoryRepository;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.ProductVariantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(ProductRepository productRepository, 
                           CategoryRepository categoryRepository, 
                           ProductVariantRepository productVariantRepository, 
                           CustomerRepository customerRepository, 
                           PasswordEncoder passwordEncoder) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productVariantRepository = productVariantRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (customerRepository.findByEmail("admin@gmail.com").isEmpty()) {
            Customer admin = new Customer();
            admin.setHoTen("Quản Trị Viên");
            admin.setEmail("admin@gmail.com");
            admin.setMatKhau(passwordEncoder.encode("123"));
            admin.setRole("ROLE_ADMIN");
            customerRepository.save(admin);
            System.out.println("--- DA THEM TAI KHOAN ADMIN: admin@gmail.com / 123 ---");
        }

        if (customerRepository.findByEmail("user@gmail.com").isEmpty()) {
            Customer testUser = new Customer();
            testUser.setHoTen("Khách Hàng Thử Nghiệm");
            testUser.setEmail("user@gmail.com");
            testUser.setMatKhau(passwordEncoder.encode("123"));
            testUser.setRole("ROLE_USER");
            testUser.setSoDienThoai("0987654321");
            testUser.setDiaChi("97 Man Thiện, Quận 9, TP.HCM");
            customerRepository.save(testUser);
            System.out.println("--- DA THEM TAI KHOAN TEST: user@gmail.com / 123 ---");
        }

        List<String> tenDanhMucs = List.of("ÁO", "QUẦN", "PHỤ KIỆN", "ÁO KHOÁC", "VÁY");
        for (String ten : tenDanhMucs) {
            if (!categoryRepository.existsByTenDanhMuc(ten)) {
                categoryRepository.save(new Category(ten));
            }
        }

        if (productRepository.count() < 2) {
            Category aoKhoac = categoryRepository.findAll().stream()
                    .filter(c -> c.getTenDanhMuc().equals("ÁO KHOÁC")).findFirst().orElse(null);
            Category vayStr = categoryRepository.findAll().stream()
                    .filter(c -> c.getTenDanhMuc().equals("VÁY")).findFirst().orElse(null);

            if (aoKhoac != null) {
                Product p1 = new Product();
                p1.setTenSanPham("Blazer Linen Cấu Trúc");
                p1.setMaSKU("BLZ-LINEN");
                p1.setMoTa("Sự tĩnh lặng tuyệt đối với kiểu dáng suông dài, tôn lên vẻ thanh lịch.");
                p1.setGiaNiemYet(new BigDecimal("2450000.00"));
                p1.setCategory(aoKhoac);
                p1.setChatLieu("Vải Linen Cao Cấp");
                p1.setHinhAnh("https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=800");
                productRepository.save(p1);

                String[] colors = {"Đen", "Trắng", "Xám"};
                KichThuoc[] sizes = {KichThuoc.S, KichThuoc.M, KichThuoc.L};
                for (int i = 0; i < 3; i++) {
                    ProductVariant v = new ProductVariant();
                    v.setProduct(p1);
                    v.setKichThuoc(sizes[i]);
                    v.setMauSac(colors[i]);
                    v.setSoLuongTon(20);
                    productVariantRepository.save(v);
                }
            }

            if (vayStr != null) {
                Product p2 = new Product();
                p2.setTenSanPham("Váy Lụa Slip Dress");
                p2.setMaSKU("VY-LUA");
                p2.setMoTa("Thanh lịch tối giản với chất liệu lụa tơ tằm.");
                p2.setGiaNiemYet(new BigDecimal("1890000.00"));
                p2.setCategory(vayStr);
                p2.setChatLieu("Lụa tơ tằm");
                p2.setHinhAnh("https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=800");
                productRepository.save(p2);

                ProductVariant v = new ProductVariant();
                v.setProduct(p2);
                v.setKichThuoc(KichThuoc.M);
                v.setMauSac("Kem");
                v.setSoLuongTon(15);
                productVariantRepository.save(v);
            }
            System.out.println("--- DA KHOI TAO DU LIEU MAU THANH CONG ---");
        }
    }
}
