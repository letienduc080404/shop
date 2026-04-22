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
}
