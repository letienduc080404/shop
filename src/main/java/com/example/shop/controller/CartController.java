package com.example.shop.controller;

import com.example.shop.dto.CartItem;
import com.example.shop.entity.Product;
import com.example.shop.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CartController {

    private final ProductService productService;

    public CartController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
        }
        
        double total = cart.stream().mapToDouble(item -> item.getGia() * item.getSoLuong()).sum();
        model.addAttribute("cart", cart);
        model.addAttribute("total", total);

        // Fetch a few recommended products
        List<Product> allProducts = productService.getAllProducts();
        List<Product> recommendedProducts = allProducts.stream().limit(2).toList();
        model.addAttribute("recommendedProducts", recommendedProducts);
        
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam(value = "kichThuoc", required = false, defaultValue = "OS") String kichThuoc,
                            @RequestParam(value = "soLuong", defaultValue = "1") int soLuong,
                            HttpSession session) {
        
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
        }

        Product product = productService.getProductById(productId);
        if (product != null) {
            boolean exists = false;
            for (CartItem item : cart) {
                if (item.getIdSanPham().equals(productId) && item.getKichThuoc().equals(kichThuoc)) {
                    item.setSoLuong(item.getSoLuong() + soLuong);
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                CartItem newItem = new CartItem();
                newItem.setIdSanPham(product.getIdSanPham());
                newItem.setTenSanPham(product.getTenSanPham());
                newItem.setGia(product.getGiaNiemYet().doubleValue());
                newItem.setSoLuong(soLuong);
                newItem.setKichThuoc(kichThuoc);
                newItem.setHinhAnh(product.getImageUrl());
                cart.add(newItem);
            }
        }
        
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam("index") int index, HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null && index >= 0 && index < cart.size()) {
            cart.remove(index);
            session.setAttribute("cart", cart);
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam("index") int index, @RequestParam("action") String action, HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null && index >= 0 && index < cart.size()) {
            CartItem item = cart.get(index);
            if ("plus".equals(action)) {
                item.setSoLuong(item.getSoLuong() + 1);
            } else if ("minus".equals(action)) {
                item.setSoLuong(item.getSoLuong() - 1);
                if (item.getSoLuong() <= 0) {
                    cart.remove(index);
                }
            }
            session.setAttribute("cart", cart);
        }
        return "redirect:/cart";
    }
}
