package com.example.shop.controller;

import com.example.shop.dto.admin.DashboardSummaryDto;
import com.example.shop.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {
    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Render trang tổng quan doanh thu (dashboard).
     * Dữ liệu được lấy từ service để tránh nhồi logic vào controller.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardSummaryDto dashboard = dashboardService.buildAdminDashboardSummary();
        model.addAttribute("dashboard", dashboard);
        return "admin/dashboard";
    }

    @GetMapping("/settings")
    public String settings() {
        return "admin/settings";
    }
}

