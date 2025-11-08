package com.example.notification.kafka;

import com.example.notification.dto.UserOperationMessage;
import com.example.notification.service.EmailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private final EmailService emailService;

    public NotificationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "${app.kafka.topic:user-events}", groupId = "${spring.kafka.consumer.group-id:notification-group}")
    public void listen(UserOperationMessage msg) {
        System.out.println("Получено сообщение: " + msg);
        if (msg == null) return;
        String op = msg.getOperation();
        String email = msg.getEmail();
        if (email == null || email.isBlank()) return;

        if ("CREATED".equalsIgnoreCase(op)) {
            emailService.sendSimple(email, "Аккаунт создан", "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.");
        } else if ("DELETED".equalsIgnoreCase(op)) {
            emailService.sendSimple(email, "Аккаунт удалён", "Здравствуйте! Ваш аккаунт был удалён.");
        } else {
            // Unknown operation - ignore or log
            System.out.println("Unknown operation received: " + op);
        }
    }
}
