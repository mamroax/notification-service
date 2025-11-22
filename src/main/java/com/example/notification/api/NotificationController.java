package com.example.notification.api;

import com.example.notification.dto.UserOperationMessage;
import com.example.notification.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final EmailService emailService;

    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> send(@RequestBody UserOperationMessage msg) {
        if (msg == null || msg.getEmail() == null || msg.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Missing message or email");
        }
        String op = msg.getOperation();
        if ("CREATED".equalsIgnoreCase(op)) {
            emailService.sendSimple(msg.getEmail(), "Аккаунт создан", "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.");
        } else if ("DELETED".equalsIgnoreCase(op)) {
            emailService.sendSimple(msg.getEmail(), "Аккаунт удалён", "Здравствуйте! Ваш аккаунт был удалён.");
        } else {
            return ResponseEntity.badRequest().body("Unknown operation");
        }
        return ResponseEntity.ok("Sent");
    }
}
