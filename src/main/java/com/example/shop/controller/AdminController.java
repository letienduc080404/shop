package com.example.shop.controller;

import com.example.shop.entity.Product;
import com.example.shop.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;

    public AdminController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public String listProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        Map<Long, Integer> productStocks = productService.getProductStockMap(products);

        model.addAttribute("products", products);
        model.addAttribute("productStocks", productStocks);
        model.addAttribute("totalCount", products.size());
        
        long lowStockCount = productStocks.values().stream().filter(s -> s > 0 && s < 10).count();
        model.addAttribute("lowStockCount", lowStockCount);
        
        long outOfStockCount = productStocks.values().stream().filter(s -> s == 0).count();
        model.addAttribute("outOfStockCount", outOfStockCount);
        
        model.addAttribute("activeCount", products.size() - outOfStockCount);
        return "admin/products";
    }

    @GetMapping("/product/add")
    public String addProductForm(Model model) {
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("kichThuocs", com.example.shop.entity.enums.KichThuoc.values());
        return "admin/add-product";
    }

    @org.springframework.web.bind.annotation.PostMapping("/product/add")
    public String addProduct(@org.springframework.web.bind.annotation.RequestParam("tenSanPham") String tenSanPham,
                             @org.springframework.web.bind.annotation.RequestParam("idDanhMuc") Long idDanhMuc,
                             @org.springframework.web.bind.annotation.RequestParam("giaNiemYet") java.math.BigDecimal giaNiemYet,
                             @org.springframework.web.bind.annotation.RequestParam("moTa") String moTa,
                             @org.springframework.web.bind.annotation.RequestParam("mauSacs") String mauSacs,
                             @org.springframework.web.bind.annotation.RequestParam("sizes") List<String> sizes,
                             @org.springframework.web.bind.annotation.RequestParam("files") org.springframework.web.multipart.MultipartFile[] files) {
        
        productService.createProduct(tenSanPham, idDanhMuc, giaNiemYet, moTa, mauSacs, sizes, files);
        return "redirect:/admin/products";
    }

    @org.springframework.web.bind.annotation.GetMapping("/product/delete/{id}")
    public String deleteProduct(@org.springframework.web.bind.annotation.PathVariable("id") Long id) {
        productService.deleteProductById(id);
        return "redirect:/admin/products";
    }
}
