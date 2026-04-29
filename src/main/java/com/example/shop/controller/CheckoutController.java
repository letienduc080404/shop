package com.example.shop.controller;

import com.example.shop.dto.CartItem;
import com.example.shop.entity.Customer;
import com.example.shop.entity.Order;
import com.example.shop.model.DiscountCode;
import com.example.shop.entity.enums.PhuongThucThanhToan;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.service.DiscountCodeService;
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
import java.math.BigDecimal;

@Controller
public class CheckoutController {

    private final OrderService orderService;
    private final CustomerRepository customerRepository;
    private final DiscountCodeService discountCodeService;

    public CheckoutController(OrderService orderService, CustomerRepository customerRepository, DiscountCodeService discountCodeService) {
        this.orderService = orderService;
        this.customerRepository = customerRepository;
        this.discountCodeService = discountCodeService;
    }

    @GetMapping("/checkout")
    @SuppressWarnings("unchecked")
    public String viewCheckout(@RequestParam(value = "promoCode", required = false) String promoCode,
                               HttpSession session, Model model) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Customer customer = customerRepository.findByEmail(email).orElse(null);

        BigDecimal total = cart.stream()
                .map(item -> BigDecimal.valueOf(item.getGia()).multiply(BigDecimal.valueOf(item.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DiscountCode appliedDiscountCode = discountCodeService.findActiveByCode(promoCode).orElse(null);
        BigDecimal discount = discountCodeService.calculateDiscount(appliedDiscountCode, total);
        BigDecimal finalTotal = total.subtract(discount).max(BigDecimal.ZERO);

        model.addAttribute("cart", cart);
        model.addAttribute("total", total);
        model.addAttribute("discount", discount);
        model.addAttribute("finalTotal", finalTotal);
        model.addAttribute("customer", customer);
        model.addAttribute("promoCode", appliedDiscountCode != null ? appliedDiscountCode.getCode() : "");
        model.addAttribute("appliedDiscountCode", appliedDiscountCode);
        if (promoCode != null && !promoCode.isBlank() && appliedDiscountCode == null) {
            model.addAttribute("promoError", "Mã ưu đãi không hợp lệ hoặc đã bị tắt.");
        }

        return "checkout";
    }

    @PostMapping("/checkout")
    @SuppressWarnings("unchecked")
    public String processCheckout(@RequestParam("diaChi") String diaChi,
                                  @RequestParam("soDienThoai") String soDienThoai,
                                  @RequestParam("payment") String payment,
                                  @RequestParam(value = "promoCode", required = false) String promoCode,
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

        BigDecimal subtotal = cart.stream()
                .map(item -> BigDecimal.valueOf(item.getGia()).multiply(BigDecimal.valueOf(item.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        DiscountCode appliedDiscountCode = discountCodeService.findActiveByCode(promoCode).orElse(null);
        BigDecimal discount = discountCodeService.calculateDiscount(appliedDiscountCode, subtotal);

        Order order = orderService.createOrder(
                customer, cart, diaChi, soDienThoai, phuongThuc,
                discount,
                appliedDiscountCode != null ? appliedDiscountCode.getCode() : null);
        
        // Xoá giỏ hàng sau khi đặt
        session.setAttribute("cart", new ArrayList<CartItem>());
        
        model.addAttribute("order", order);
        return "order-success";
    }
}
