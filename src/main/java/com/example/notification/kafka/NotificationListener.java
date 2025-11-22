package com.example.notification.kafka;


import com.example.notification.dto.UserEvent;
import com.example.notification.service.EmailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public class NotificationListener {


    private final EmailService emailService;


    public NotificationListener(EmailService emailService) {
        this.emailService = emailService;
    }


    @KafkaListener(topics = "user-events", groupId = "notification-group")
    public void listen(UserEvent msg) {
        System.out.println("Message received: " + msg);
        if (msg == null) return;
        String op = msg.getOperation();
        String email = msg.getEmail();
        if (email == null || email.isBlank()) return;


        if ("CREATED".equalsIgnoreCase(op)) {
            emailService.sendSimple(email, "Account created", "Hello! Your account on the site has been successfully created.");
        } else if ("DELETED".equalsIgnoreCase(op)) {
            emailService.sendSimple(email, "Account deleted", "Hello! Your account has been deleted.");
        } else {
            System.out.println("Unknown operation received: " + op);
        }
    }
}