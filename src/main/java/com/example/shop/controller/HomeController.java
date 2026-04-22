package com.example.shop.controller;

import com.example.shop.repository.CategoryRepository;
import com.example.shop.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    public HomeController(ProductService productService, CategoryRepository categoryRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "index";
    }

    @GetMapping("/shop")
    public String shop(@RequestParam(value = "category", required = false) Long categoryId, Model model) {
        if (categoryId != null) {
            model.addAttribute("products", productService.getProductsByCategory(categoryId));
        } else {
            model.addAttribute("products", productService.getAllProducts());
        }
        model.addAttribute("categories", categoryRepository.findAll());
        return "shop";
    }
}
