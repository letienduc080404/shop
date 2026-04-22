package com.example.shop.controller;

import java.util.List;

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

        List<Product> allProducts = productService.getAllProducts();
        List<Product> recommendations = allProducts.stream()
                .filter(p -> !p.getIdSanPham().equals(id))
                .limit(4)
                .toList();

        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("recommendations", recommendations);

        return "product-detail";
    }
}
