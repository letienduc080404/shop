package com.example.shop.service;

import com.example.shop.entity.Customer;
import com.example.shop.repository.CustomerRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final CustomerRepository customerRepository;

    public UserDetailsServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println(">>> DANG KIEM TRA DANG NHAP CHO EMAIL: " + email);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println(">>> KHONG TIM THAY EMAIL NAY TRONG DATABASE!");
                    return new UsernameNotFoundException("Không tìm thấy khách hàng với email: " + email);
                });

        System.out.println(">>> TIM THAY NGUOI DUNG: " + customer.getHoTen() + " - DANG KIEM TRA MAT KHAU...");
        return new User(
                customer.getEmail(),
                customer.getMatKhau(),
                new ArrayList<>()
        );
    }
}
