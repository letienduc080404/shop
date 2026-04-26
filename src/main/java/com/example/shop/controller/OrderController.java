package com.example.shop.controller;

import com.example.shop.entity.Order;
import com.example.shop.repository.OrderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class OrderController {

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/order/track/{maDonHang}")
    public String trackOrder(@PathVariable("maDonHang") String maDonHang, Model model) {
        Order order = orderRepository.findByMaDonHang(maDonHang).orElse(null);
        if (order == null) {
            return "redirect:/";
        }
        model.addAttribute("order", order);
        return "tracking";
    }

    @GetMapping("/orders")
    public String myOrders(Model model, java.security.Principal principal) {
        if (principal == null) return "redirect:/login";
        String email = principal.getName();
        model.addAttribute("orders", orderRepository.findByCustomer_EmailOrderByNgayDatDesc(email));
        return "orders";
    }

    @org.springframework.web.bind.annotation.PostMapping("/order/cancel")
    public String cancelOrder(@org.springframework.web.bind.annotation.RequestParam("maDonHang") String maDonHang, java.security.Principal principal) {
        Order order = orderRepository.findByMaDonHang(maDonHang).orElse(null);
        if (order != null && principal != null && order.getCustomer().getEmail().equals(principal.getName())) {
            if (order.getTrangThaiDonHang() == com.example.shop.entity.enums.TrangThaiDonHang.ChoXuLy) {
                order.setTrangThaiDonHang(com.example.shop.entity.enums.TrangThaiDonHang.DaHuy);
                order.setGhiChu("đơn do khách hàng hủy");
                orderRepository.save(order);
            }
        }
        return "redirect:/order/track/" + maDonHang;
    }

    @org.springframework.web.bind.annotation.PostMapping("/order/complete")
    public String completeOrder(@org.springframework.web.bind.annotation.RequestParam("maDonHang") String maDonHang, java.security.Principal principal) {
        Order order = orderRepository.findByMaDonHang(maDonHang).orElse(null);
        if (order != null && principal != null && order.getCustomer().getEmail().equals(principal.getName())) {
            if (order.getTrangThaiDonHang() == com.example.shop.entity.enums.TrangThaiDonHang.DangGiao) {
                order.setTrangThaiDonHang(com.example.shop.entity.enums.TrangThaiDonHang.HoanThanh);
                orderRepository.save(order);
            }
        }
        return "redirect:/order/track/" + maDonHang;
    }
}
