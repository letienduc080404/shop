package com.example.shop.controller;

import com.example.shop.entity.Customer;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.service.GoogleDriveService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;

@Controller
public class AuthController {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleDriveService googleDriveService;

    public AuthController(CustomerRepository customerRepository, PasswordEncoder passwordEncoder, GoogleDriveService googleDriveService) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.googleDriveService = googleDriveService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String hoTen,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String soDienThoai,
            @RequestParam(required = false) String diaChi,
            Model model) {

        if (customerRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email đã tồn tại.");
            return "register";
        }

        Customer customer = new Customer();
        customer.setHoTen(hoTen);
        customer.setEmail(email);
        customer.setMatKhau(passwordEncoder.encode(password));
        customer.setSoDienThoai(soDienThoai);
        customer.setDiaChi(diaChi);

        customerRepository.save(customer);

        return "redirect:/login?registered=true";
    }

    @GetMapping("/account")
    public String accountPage(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        Customer customer = customerRepository.findByEmail(principal.getName()).orElse(null);
        if (customer == null) return "redirect:/login";
        model.addAttribute("customer", customer);
        return "account";
    }

    @PostMapping("/account/update")
    public String updateAccount(@RequestParam String hoTen,
                                @RequestParam(required = false) String soDienThoai,
                                @RequestParam(required = false) String diaChi,
                                @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        Customer customer = customerRepository.findByEmail(principal.getName()).orElse(null);
        if (customer != null) {
            customer.setHoTen(hoTen);
            customer.setSoDienThoai(soDienThoai);
            customer.setDiaChi(diaChi);

            if (avatarFile != null && !avatarFile.isEmpty()) {
                String contentType = avatarFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    redirectAttributes.addFlashAttribute("error", "File ảnh không hợp lệ. Vui lòng chọn ảnh PNG/JPG/WebP.");
                    return "redirect:/account";
                }
                try {
                    String oldAvatar = customer.getAnhDaiDien();
                    String uploadedFileId = googleDriveService.uploadFile(avatarFile);
                    customer.setAnhDaiDien(uploadedFileId);

                    if (oldAvatar != null && !oldAvatar.isBlank()) {
                        googleDriveService.deleteFile(oldAvatar);
                    }
                } catch (IOException ex) {
                    redirectAttributes.addFlashAttribute("error", "Không thể tải ảnh đại diện. Vui lòng thử lại.");
                    return "redirect:/account";
                }
            }

            customerRepository.save(customer);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        }
        return "redirect:/account";
    }
}
