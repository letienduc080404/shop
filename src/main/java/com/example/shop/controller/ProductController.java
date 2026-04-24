package com.example.shop.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.shop.entity.Product;
import com.example.shop.entity.ProductVariant;
import com.example.shop.service.ProductService;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable("id") Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/shop";
        }

        List<ProductVariant> variants = productService.getVariantsByProduct(product);
        List<Map<String, Object>> variantList = variants.stream().map(v -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", v.getIdBienThe());
            map.put("color", v.getMauSac());
            map.put("size", v.getKichThuoc().toString());
            map.put("stock", v.getSoLuongTon());
            return map;
        }).toList();

        List<Product> allProducts = productService.getAllProducts();
        List<Product> recommendations = allProducts.stream()
                .filter(p -> !p.getIdSanPham().equals(id))
                .limit(4)
                .toList();

        model.addAttribute("product", product);
        model.addAttribute("variants", variantList); // Gửi dữ liệu sạch sang view
        model.addAttribute("rawVariants", variants); // Để render nút bấm
        model.addAttribute("recommendations", recommendations);

        return "product-detail";
    }
}
