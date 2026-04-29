package com.example.shop.controller;

import com.example.shop.entity.Product;
import com.example.shop.service.ProductService;
import com.example.shop.entity.enums.KichThuoc;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Collections;
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
    public String listProducts(Model model,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "category", required = false) Long categoryId) {
        List<Product> products = productService.searchProducts(search, categoryId);
        Map<Long, Integer> productStocks = productService.getProductStockMap(products);

        model.addAttribute("products", products);
        model.addAttribute("productStocks", productStocks);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("totalCount", products.size());
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentCategory", categoryId);

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
        model.addAttribute("kichThuocs", KichThuoc.values());
        return "admin/add-product";
    }

    @PostMapping("/product/add")
    public String addProduct(@RequestParam("tenSanPham") String tenSanPham,
            @RequestParam("idDanhMuc") Long idDanhMuc,
            @RequestParam("giaNiemYet") BigDecimal giaNiemYet,
            @RequestParam(name = "giaKhuyenMai", required = false) BigDecimal giaKhuyenMai,
            @RequestParam("moTa") String moTa,
            @RequestParam(name = "variantSizes", required = false) List<String> variantSizes,
            @RequestParam(name = "variantColors", required = false) List<String> variantColors,
            @RequestParam(name = "variantStocks", required = false) List<Integer> variantStocks,
            @RequestParam(name = "variantCosts", required = false) List<BigDecimal> variantCosts,
            @RequestParam("files") MultipartFile[] files) {

        productService.createProduct(
                tenSanPham, idDanhMuc, giaNiemYet, giaKhuyenMai, moTa,
                variantSizes == null ? Collections.emptyList() : variantSizes,
                variantColors == null ? Collections.emptyList() : variantColors,
                variantStocks == null ? Collections.emptyList() : variantStocks,
                variantCosts == null ? Collections.emptyList() : variantCosts,
                files);
        return "redirect:/admin/products";
    }

    @GetMapping("/product/edit/{id}")
    public String editProductForm(@PathVariable("id") Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null)
            return "redirect:/admin/products";

        model.addAttribute("product", product);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("variants", productService.getVariantsByProduct(product));
        return "admin/edit-product";
    }

    @PostMapping("/product/edit/{id}")
    public String updateProduct(@PathVariable("id") Long id,
            @RequestParam("tenSanPham") String tenSanPham,
            @RequestParam("idDanhMuc") Long idDanhMuc,
            @RequestParam("giaNiemYet") BigDecimal giaNiemYet,
            @RequestParam(name = "giaKhuyenMai", required = false) BigDecimal giaKhuyenMai,
            @RequestParam("moTa") String moTa,
            @RequestParam(name = "variantIds", required = false) List<Long> variantIds,
            @RequestParam(name = "variantStocks", required = false) List<Integer> variantStocks,
            @RequestParam(name = "variantCosts", required = false) List<BigDecimal> variantCosts,
            @RequestParam("files") MultipartFile[] files) {

        productService.updateProduct(
                id, tenSanPham, idDanhMuc, giaNiemYet, giaKhuyenMai, moTa,
                variantIds == null ? Collections.emptyList() : variantIds,
                variantStocks == null ? Collections.emptyList() : variantStocks,
                variantCosts == null ? Collections.emptyList() : variantCosts,
                files);
        return "redirect:/admin/products";
    }

    @GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProductById(id);
            redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm thành công.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }
}
