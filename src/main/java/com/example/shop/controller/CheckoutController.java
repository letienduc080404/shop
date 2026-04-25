package com.example.shop.controller;

import com.example.shop.dto.CartItem;
import com.example.shop.entity.Customer;
import com.example.shop.entity.Order;
import com.example.shop.entity.enums.PhuongThucThanhToan;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CheckoutController {

    private final OrderService orderService;
    private final CustomerRepository customerRepository;

    public CheckoutController(OrderService orderService, CustomerRepository customerRepository) {
        this.orderService = orderService;
        this.customerRepository = customerRepository;
    }

    @GetMapping("/checkout")
    @SuppressWarnings("unchecked")
    public String viewCheckout(HttpSession session, Model model) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Customer customer = customerRepository.findByEmail(email).orElse(null);

        double total = cart.stream().mapToDouble(item -> item.getGia() * item.getSoLuong()).sum();

        model.addAttribute("cart", cart);
        model.addAttribute("total", total);
        model.addAttribute("customer", customer);

        return "checkout";
    }

    @PostMapping("/checkout")
    @SuppressWarnings("unchecked")
    public String processCheckout(@RequestParam("diaChi") String diaChi,
                                  @RequestParam("soDienThoai") String soDienThoai,
                                  @RequestParam("payment") String payment,
                                  HttpSession session,
                                  Model model) {
        
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Customer customer = customerRepository.findByEmail(email).orElseThrow();

        PhuongThucThanhToan phuongThuc;
        try {
            phuongThuc = PhuongThucThanhToan.valueOf(payment);
        } catch (IllegalArgumentException e) {
            phuongThuc = PhuongThucThanhToan.COD; // Mặc định
        }

        Order order = orderService.createOrder(customer, cart, diaChi, soDienThoai, phuongThuc);
        
        // Xoá giỏ hàng sau khi đặt
        session.setAttribute("cart", new ArrayList<CartItem>());
        
        model.addAttribute("order", order);
        return "order-success";
    }
}
