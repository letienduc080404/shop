package com.example.shop.controller;

import com.example.shop.dto.admin.CustomerAdminRowDto;
import com.example.shop.entity.Customer;
import com.example.shop.entity.Order;
import com.example.shop.entity.enums.HangThanhVien;
import com.example.shop.service.CustomerService;
import com.example.shop.service.GoogleDriveService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminCustomerController {

    private final CustomerService customerService;
    private final GoogleDriveService googleDriveService;

    public AdminCustomerController(CustomerService customerService, GoogleDriveService googleDriveService) {
        this.customerService = customerService;
        this.googleDriveService = googleDriveService;
    }

    @GetMapping("/customers")
    public String listCustomers(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "hang", required = false) String hang,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "selectedId", required = false) Long selectedId,
            Model model
    ) {
        HangThanhVien selectedHang = parseHangThanhVien(hang);
        Page<CustomerAdminRowDto> customersPage = customerService.getCustomersForAdmin(q, selectedHang, page, size);

        long totalCustomers = customerService.getTotalCustomers();
        long diamondCustomers = customerService.getDiamondCustomers();
        BigDecimal avgOrderValue = customerService.getAverageOrderValue();
        if (avgOrderValue == null) avgOrderValue = BigDecimal.ZERO;
        
        double retentionRate = customerService.getRetentionRatePercent(90);

        Customer selectedCustomer = selectedId == null ? null : customerService.getCustomerById(selectedId);
        List<Order> selectedCustomerOrders = selectedCustomer == null
                ? List.of()
                : customerService.getLatestOrdersOfCustomer(selectedCustomer.getIdKhachHang(), 5);

        model.addAttribute("page", customersPage);
        model.addAttribute("q", q);
        model.addAttribute("hang", selectedHang != null ? selectedHang.name() : "");
        model.addAttribute("hangOptions", HangThanhVien.values());
        model.addAttribute("currentTab", "customers");

        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("diamondCustomers", diamondCustomers);
        model.addAttribute("avgOrderValue", avgOrderValue);
        model.addAttribute("retentionRate", retentionRate);

        model.addAttribute("selectedCustomer", selectedCustomer);
        model.addAttribute("selectedCustomerOrders", selectedCustomerOrders);

        return "admin/customer";
    }

    private HangThanhVien parseHangThanhVien(String hang) {
        if (hang == null || hang.isBlank()) {
            return null;
        }
        try {
            return HangThanhVien.valueOf(hang.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @PostMapping("/customers/delete/{id}")
    public String deleteCustomer(
            @PathVariable Long id,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "hang", required = false) String hang,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Customer customer = customerService.getCustomerById(id);
            boolean deleted = customerService.deleteCustomerById(id);
            if (deleted) {
                if (customer != null && customer.getAnhDaiDien() != null && !customer.getAnhDaiDien().isBlank()) {
                    googleDriveService.deleteFile(customer.getAnhDaiDien());
                }
                redirectAttributes.addFlashAttribute("success", "Đã xóa khách hàng thành công.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy khách hàng cần xóa.");
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa khách hàng. Vui lòng thử lại.");
        }

        return buildCustomersRedirectUrl(q, hang, page, size);
    }

    private String buildCustomersRedirectUrl(String q, String hang, int page, int size) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/admin/customers")
                .queryParam("page", Math.max(page, 0))
                .queryParam("size", Math.max(size, 1));
        if (q != null && !q.isBlank()) {
            builder.queryParam("q", q);
        }
        if (hang != null && !hang.isBlank()) {
            builder.queryParam("hang", hang);
        }
        return "redirect:" + builder.toUriString();
    }
}

