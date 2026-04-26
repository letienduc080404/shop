package com.example.shop.controller;

import com.example.shop.entity.Order;
import com.example.shop.entity.enums.TrangThaiDonHang;
import com.example.shop.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.YearMonth;

@Controller
@RequestMapping("/admin")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public String listOrders(
            @RequestParam(value = "status", required = false) TrangThaiDonHang status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "ngayDat"));

        Page<Order> ordersPage = orderService.getOrdersForAdmin(status, pageable);

        long totalOrders = orderService.getTotalOrdersCount();
        long pendingCount = orderService.getOrdersCountByStatus(TrangThaiDonHang.ChoXuLy);
        long cancelledCount = orderService.getOrdersCountByStatus(TrangThaiDonHang.DaHuy);

        BigDecimal revenueThisMonth = orderService.getRevenueForMonth(YearMonth.now());
        double cancelRate = totalOrders == 0 ? 0.0 : (cancelledCount * 100.0) / totalOrders;

        model.addAttribute("page", ordersPage);
        model.addAttribute("status", status);

        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("revenueThisMonth", revenueThisMonth);
        model.addAttribute("cancelRate", cancelRate);

        return "admin/order";
    }

    @PostMapping("/order/prepare")
    public String prepareOrder(@RequestParam("id") Long id,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderById(id);
            if (order != null && order.getTrangThaiDonHang() == TrangThaiDonHang.ChoXuLy) {
                order.setTrangThaiDonHang(TrangThaiDonHang.DangGiao);
                orderService.saveOrder(order);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi: " + e.getMessage() + " | " + e.getClass().getName());
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/order/cancel")
    public String cancelOrder(@RequestParam("id") Long id, @RequestParam("reason") String reason) {
        Order order = orderService.getOrderById(id);
        if (order != null && order.getTrangThaiDonHang() == TrangThaiDonHang.ChoXuLy) {
            order.setTrangThaiDonHang(TrangThaiDonHang.DaHuy);
            order.setGhiChu("Admin hủy: " + reason);
            orderService.saveOrder(order);
        }
        return "redirect:/admin/orders";
    }
}
