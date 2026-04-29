package com.example.shop.chatbot;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatBotController {

    private final ChatBotService chatBotService;

    public ChatBotController(ChatBotService chatBotService) {
        this.chatBotService = chatBotService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String message = request != null ? request.getMessage() : null;
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ChatResponse("Bạn vui lòng nhập nội dung câu hỏi."));
        }
        return ResponseEntity.ok(chatBotService.ask(request));
    }
}
