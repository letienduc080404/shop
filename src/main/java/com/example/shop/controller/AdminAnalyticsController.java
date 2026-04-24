package com.example.shop.controller;

import com.example.shop.dto.admin.AnalyticsSummaryDto;
import com.example.shop.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
public class AdminAnalyticsController {
    private final AnalyticsService analyticsService;

    public AdminAnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Trang Analytics: hỗ trợ lọc theo ngày.
     * Ví dụ: /admin/analytics?from=2024-10-01&to=2024-10-31
     */
    @GetMapping("/analytics")
    public String analytics(
            Model model,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        AnalyticsSummaryDto analytics = analyticsService.buildAnalytics(from, to);
        model.addAttribute("analytics", analytics);
        return "admin/analytics";
    }
}

