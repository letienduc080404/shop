package com.example.shop.controller;

import com.example.shop.entity.SupportMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {

    // Giới hạn để tránh RAM overflow
    private static final int MAX_MESSAGES_PER_ORDER = 200;
    private static final int MAX_ACTIVE_ORDERS = 100;
    private static final int MAX_MESSAGE_LENGTH = 1000;

    // Sử dụng bộ nhớ tạm thời (RAM) để lưu tin nhắn thay vì Database
    private static final Map<String, List<SupportMessage>> IN_MEMORY_DB = new ConcurrentHashMap<>();

    // --- API cho giao diện người dùng và Admin (AJAX polling) ---
    @GetMapping("/api/chat/active")
    @ResponseBody
    public ResponseEntity<java.util.Set<String>> getActiveChats() {
        return ResponseEntity.ok(IN_MEMORY_DB.keySet());
    }

    @GetMapping("/api/chat/{orderCode}")
    @ResponseBody
    public ResponseEntity<List<SupportMessage>> getMessages(@PathVariable String orderCode) {
        // Validate orderCode
        if (orderCode == null || orderCode.isBlank() || orderCode.length() > 50) {
            return ResponseEntity.badRequest().build();
        }
        List<SupportMessage> messages = IN_MEMORY_DB.getOrDefault(orderCode, new ArrayList<>());
        // Trả về bản copy để tránh race condition
        return ResponseEntity.ok(new ArrayList<>(messages));
    }

    @PostMapping("/api/chat/{orderCode}")
    @ResponseBody
    public ResponseEntity<SupportMessage> sendMessage(@PathVariable String orderCode,
            @RequestParam String role,
            @RequestParam String message) {

        // Validate orderCode
        if (orderCode == null || orderCode.isBlank() || orderCode.length() > 50) {
            return ResponseEntity.badRequest().build();
        }
        // Validate role
        if (!"USER".equals(role) && !"ADMIN".equals(role)) {
            return ResponseEntity.badRequest().build();
        }
        // Validate và trim message
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String trimmedMessage = message.trim();
        if (trimmedMessage.length() > MAX_MESSAGE_LENGTH) {
            trimmedMessage = trimmedMessage.substring(0, MAX_MESSAGE_LENGTH);
        }

        // Không tạo thêm đơn mới nếu đã đạt giới hạn tối đa
        if (!IN_MEMORY_DB.containsKey(orderCode) && IN_MEMORY_DB.size() >= MAX_ACTIVE_ORDERS) {
            return ResponseEntity.status(429).build(); // Too Many Requests
        }

        SupportMessage sm = new SupportMessage(orderCode, role, trimmedMessage);

        // Synchronized để tránh race condition khi nhiều request cùng lúc
        IN_MEMORY_DB.compute(orderCode, (k, list) -> {
            if (list == null) list = new ArrayList<>();
            list.add(sm);
            // Xóa tin nhắn cũ nhất nếu vượt giới hạn
            if (list.size() > MAX_MESSAGES_PER_ORDER) {
                list.remove(0);
            }
            return list;
        });

        return ResponseEntity.ok(sm);
    }

    // Admin xóa chat của một đơn (optional cleanup)
    @DeleteMapping("/api/chat/{orderCode}")
    @ResponseBody
    public ResponseEntity<Void> clearChat(@PathVariable String orderCode) {
        IN_MEMORY_DB.remove(orderCode);
        return ResponseEntity.ok().build();
    }

    // --- Trang Admin quản lý Chat ---
    @GetMapping("/admin/chat")
    public String adminChat(Model model) {
        model.addAttribute("activeOrderCodes", IN_MEMORY_DB.keySet());
        return "admin-chat";
    }
}
