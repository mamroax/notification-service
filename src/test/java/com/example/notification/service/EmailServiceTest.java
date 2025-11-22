package com.example.notification.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Test
    void testSendSimple_success() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        EmailService service = new EmailService(mailSender);

        service.sendSimple("test@mail.com", "Hello", "Body");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendSimple_exceptionDoesNotThrow() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("FAIL"));

        EmailService service = new EmailService(mailSender);

        // метод НЕ должен пробрасывать исключение
        service.sendSimple("x@y.z", "S", "BODY");
    }
}
