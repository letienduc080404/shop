package com.example.shop.chatbot;

import com.example.shop.entity.Order;
import com.example.shop.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
public class ChatBotSupportController {

    private final OrderRepository orderRepository;

    public ChatBotSupportController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<String>> myOrders(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return ResponseEntity.status(401).build();
        }
        List<String> orderCodes = orderRepository.findByCustomer_EmailOrderByNgayDatDesc(principal.getName()).stream()
                .map(Order::getMaDonHang)
                .filter(code -> code != null && !code.isBlank())
                .limit(15)
                .toList();
        return ResponseEntity.ok(orderCodes);
    }
}
