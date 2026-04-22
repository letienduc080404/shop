package com.example.shop.controller;

import com.example.shop.entity.Customer;
import com.example.shop.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
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
}
