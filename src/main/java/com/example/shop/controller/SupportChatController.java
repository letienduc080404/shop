package com.example.shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class SupportChatController {

    @GetMapping("/support/chat/{conversationKey}")
    public String supportChat(@PathVariable String conversationKey, Model model) {
        // Validate để tránh đưa chuỗi lạ vào view
        if (conversationKey == null || conversationKey.isBlank() || conversationKey.length() > 80) {
            return "redirect:/";
        }

        model.addAttribute("conversationKey", conversationKey.trim());
        return "support-chat";
    }
}

