package com.example.shop;

import com.example.shop.entity.Category;
import com.example.shop.entity.Product;
import com.example.shop.entity.ProductVariant;
import com.example.shop.entity.enums.KichThuoc;
import com.example.shop.repository.CategoryRepository;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private com.example.shop.repository.CustomerRepository customerRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (customerRepository.findByEmail("user@gmail.com").isEmpty()) {
            com.example.shop.entity.Customer testUser = new com.example.shop.entity.Customer();
            testUser.setHoTen("Khách Hàng Thử Nghiệm");
            testUser.setEmail("user@gmail.com");
            testUser.setMatKhau(passwordEncoder.encode("123"));
            testUser.setSoDienThoai("0987654321");
            testUser.setDiaChi("97 Man Thiện, Quận 9, TP.HCM");
            customerRepository.save(testUser);
            System.out.println("--- DA THEM TAI KHOAN TEST: user@gmail.com / 123 ---");
        }

        if (productRepository.count() < 3) {
            Category ao = new Category("Áo");
            Category quan = new Category("Quần");
            categoryRepository.save(ao);
            categoryRepository.save(quan);

            Product p1 = new Product();
            p1.setTenSanPham("Blazer Linen Cấu Trúc");
            p1.setMaSKU("BLZ-LINEN");
            p1.setMoTa("Sự tĩnh lặng tuyệt đối với kiểu dáng suông dài, tôn lên vẻ thanh lịch.");
            p1.setGiaNiemYet(new BigDecimal("2450000.00"));
            p1.setCategory(ao);
            p1.setChatLieu("Vải Linen Cao Cấp");
            p1.setHinhAnh("https://lh3.googleusercontent.com/aida-public/AB6AXuAeNlzAbWJ35rW51Lw8N1-YgfrHODAyUW1zzl6KjXc7jqBUMi05DUmYkh2mNykWAzFaPE05NqqBd8IMuMStJZzgiUzIWcItROUJxsrXK1iGFoOutFw6EDx1nB9RXhuzE5-7F_FvDiDp9lWsWhIHfO62Uqu64amEhO8PpyxYrYln81-52UqTFnkZL5k8UdDGj_Ed33FZnh_XausLVexvoI5DaHTnjNG5KWDFi0Ne0s5VmWSX8J8XGd1m95IgMduLaGqM684P6uf16XDz");
            productRepository.save(p1);

            Product p2 = new Product();
            p2.setTenSanPham("Váy Lụa Slip Dress");
            p2.setMaSKU("VY-LUA");
            p2.setMoTa("Thanh lịch tối giản với chất liệu lụa tơ tằm.");
            p2.setGiaNiemYet(new BigDecimal("1890000.00"));
            p2.setCategory(quan);
            p2.setChatLieu("Lụa tơ tằm");
            p2.setHinhAnh("https://lh3.googleusercontent.com/aida-public/AB6AXuAhZYx0qVgpn6Cc8onETkxA7Rm2boi52pKTRPxwv1WPvR7VsLsE91XFgdq1apifQa9BehuwZLSZ8YDJkL3NKUmbZw1NXRnV6nTCucl6C4KtG-85x6uTNysJav8C26K97c9iAYH2OryfXjJxIccfbQLiAy5oTV-KjTbIBEpKXiqLFVGgaQGK2-tqVQBu3fmmJFKC0aP31dGUMMRxT0OJbkrOeKqCiOn15xykHMMmSjFoZQ9IB_xrpuGBQke5LDkUhZuyoMEbOYzMaiIH");
            productRepository.save(p2);

            Product p3 = new Product();
            p3.setTenSanPham("Chelsea Boots Da Bò");
            p3.setMaSKU("BOOT-CHEL");
            p3.setMoTa("Thiết kế mới, chất liệu da bò bền bỉ.");
            p3.setGiaNiemYet(new BigDecimal("3200000.00"));
            p3.setCategory(ao);
            p3.setChatLieu("Da Bò Xịn");
            p3.setHinhAnh("https://lh3.googleusercontent.com/aida-public/AB6AXuDyQ2uPUfTUx4v9F4QASlvI3iqs-Hz-st3pNjxqbn6rufnQ-hwbYs6APAGIUgTwszZYtIRGT472TuV5Ta_7DGe8L0jE5CHCTVTqBcUF2WxCmuBDnnabwO63StktpmVMVxXfrlvQtvETBeYxzBrHXUud03dGvcavIHLRtLYlROzDO0GRj3hfW7O_C7XJV0v8UFjHdBnTLZB4ezVTylN5udUWU3pHcAzF3nljcdIyJ8vOcLuNU3yPCVFJHk1c-b2t8cGB78lAQripCzvi");
            productRepository.save(p3);

            Product p4 = new Product();
            p4.setTenSanPham("T-Shirt Cotton Oversize");
            p4.setMaSKU("TSHIRT-COTTON");
            p4.setMoTa("Biểu tượng của sự thoải mái và tự do trong từng chuyển động.");
            p4.setGiaNiemYet(new BigDecimal("650000.00"));
            p4.setCategory(ao);
            p4.setChatLieu("100% Cotton");
            p4.setHinhAnh("https://lh3.googleusercontent.com/aida-public/AB6AXuBRKrbyOmkfIUGmDFOqvnqzvawI9wbzgc4oGFsAD4TJ1gFWKC6FA-Qn6Q4dlQL1IUfJRW_r63sfe8WI6NtKYkK4RyDtIkrKHxW1ZC9uh2J6Vvuc_dF8s3Fn1E5z5XZr5aikcfNe7YixCZyI1cp8j33fIiF6AbKYNf-DRLx6oNZeY7BUKZYmQJ4D3hyCFbYJJDxvRUJebmAWo-HL_ewomPrUyjQUmiytshuewWnMsd7OsG8HLs-D7Sf-Blm87H0vraXymj3rsl8-8yq7");
            productRepository.save(p4);

            // Seed Variants
            ProductVariant var1 = new ProductVariant();
            var1.setProduct(p1); var1.setKichThuoc(KichThuoc.S); var1.setMauSac("Đen"); var1.setSoLuongTon(10);
            productVariantRepository.save(var1);
            
            ProductVariant var2 = new ProductVariant();
            var2.setProduct(p1); var2.setKichThuoc(KichThuoc.M); var2.setMauSac("Đen"); var2.setSoLuongTon(15);
            productVariantRepository.save(var2);

            ProductVariant var3 = new ProductVariant();
            var3.setProduct(p2); var3.setKichThuoc(KichThuoc.L); var3.setMauSac("Xám Xám"); var3.setSoLuongTon(5);
            productVariantRepository.save(var3);

            ProductVariant var4 = new ProductVariant();
            var4.setProduct(p4); var4.setKichThuoc(KichThuoc.OS); var4.setMauSac("Đen Mun"); var4.setSoLuongTon(2);
            productVariantRepository.save(var4);

            System.out.println("--- DA THEM DU LIEU MAU KICH THUOC / MAU SAC DOMINO VAO DATABASE ---");
        }
    }
}
