package com.example.shop.controller;

import com.example.shop.entity.Product;
import com.example.shop.model.Collection;
import com.example.shop.repository.CategoryRepository;
import com.example.shop.service.ProductService;
import com.example.shop.service.CollectionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final CollectionService collectionService;

    public HomeController(ProductService productService, CategoryRepository categoryRepository, CollectionService collectionService) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.collectionService = collectionService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "index";
    }

    @GetMapping("/shop")
    public String shop(@RequestParam(value = "category", required = false) Long categoryId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "newArrivals", required = false) Boolean newArrivals,
            Model model) {
        List<Product> newArrivalsList = productService.getNewArrivals();
        List<Long> newArrivalIds = newArrivalsList.stream().map(Product::getIdSanPham).toList();

        if (Boolean.TRUE.equals(newArrivals)) {
            model.addAttribute("products", newArrivalsList);
            model.addAttribute("pageTitle", "Sản Phẩm Mới");
        } else {
            model.addAttribute("products", productService.searchProducts(keyword, categoryId));
            model.addAttribute("pageTitle", "Tất Cả Sản Phẩm");
        }
        
        model.addAttribute("newArrivalIds", newArrivalIds);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("keyword", keyword);
        return "shop";
    }

    @GetMapping("/collections")
    public String collections(Model model) {
        model.addAttribute("collections", collectionService.getAllCollections());
        return "collections";
    }

    @GetMapping("/collections/{id}")
    public String collectionDetail(@PathVariable String id, Model model) {
        Collection collection = collectionService.getCollectionById(id);
        if (collection == null) return "redirect:/collections";
        
        List<Product> products = collection.getProductIds().stream()
                .map(productService::getProductById)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        
        model.addAttribute("collection", collection);
        model.addAttribute("products", products);
        return "collection-detail";
    }
}
