package com.example.shop.controller;

import com.example.shop.model.Collection;
import com.example.shop.service.CollectionService;
import com.example.shop.service.ProductService;
import com.example.shop.service.GoogleDriveService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin/collections")
public class AdminCollectionController {
    private final CollectionService collectionService;
    private final ProductService productService;
    private final GoogleDriveService googleDriveService;

    public AdminCollectionController(CollectionService collectionService, ProductService productService, GoogleDriveService googleDriveService) {
        this.collectionService = collectionService;
        this.productService = productService;
        this.googleDriveService = googleDriveService;
    }

    @GetMapping
    public String listCollections(Model model) {
        model.addAttribute("collections", collectionService.getAllCollections());
        model.addAttribute("currentTab", "collections");
        return "admin/collection-list";
    }

    @GetMapping("/add")
    public String addCollectionForm(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("currentTab", "collections");
        return "admin/collection-form";
    }

    @PostMapping("/add")
    public String addCollection(@RequestParam String name,
                                @RequestParam String description,
                                @RequestParam("file") MultipartFile file,
                                @RequestParam(required = false) List<Long> productIds) {
        String imageUrl = "";
        if (file != null && !file.isEmpty()) {
            try {
                imageUrl = googleDriveService.uploadFile(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        collectionService.saveCollection(name, description, imageUrl, productIds);
        return "redirect:/admin/collections";
    }

    @GetMapping("/edit/{id}")
    public String editCollectionForm(@PathVariable String id, Model model) {
        model.addAttribute("collection", collectionService.getCollectionById(id));
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("currentTab", "collections");
        return "admin/collection-form";
    }

    @PostMapping("/edit/{id}")
    public String updateCollection(@PathVariable String id,
                                   @RequestParam String name,
                                   @RequestParam String description,
                                   @RequestParam(required = false) MultipartFile file,
                                   @RequestParam(required = false) List<Long> productIds) {
        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            try {
                imageUrl = googleDriveService.uploadFile(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        collectionService.updateCollection(id, name, description, imageUrl, productIds);
        return "redirect:/admin/collections";
    }

    @PostMapping("/delete/{id}")
    public String deleteCollection(@PathVariable String id) {
        collectionService.deleteCollection(id);
        return "redirect:/admin/collections";
    }
}
