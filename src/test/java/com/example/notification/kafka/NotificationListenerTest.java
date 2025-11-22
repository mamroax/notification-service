package com.example.notification.kafka;

import com.example.notification.dto.UserEvent;
import com.example.notification.service.EmailService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class NotificationListenerTest {

    @Test
    void testCreatedEvent() {
        EmailService emailService = mock(EmailService.class);
        NotificationListener listener = new NotificationListener(emailService);

        listener.listen(new UserEvent("CREATED", "test@mail.com"));

        verify(emailService).sendSimple(
                eq("test@mail.com"),
                eq("Account created"),
                anyString()
        );
    }

    @Test
    void testDeletedEvent() {
        EmailService emailService = mock(EmailService.class);
        NotificationListener listener = new NotificationListener(emailService);

        listener.listen(new UserEvent("DELETED", "test@mail.com"));

        verify(emailService).sendSimple(
                eq("test@mail.com"),
                eq("Account deleted"),
                anyString()
        );
    }

    @Test
    void testNullMessage() {
        EmailService emailService = mock(EmailService.class);
        NotificationListener listener = new NotificationListener(emailService);

        listener.listen(null);

        verifyNoInteractions(emailService);
    }

    @Test
    void testUnknownOperation() {
        EmailService emailService = mock(EmailService.class);
        NotificationListener listener = new NotificationListener(emailService);

        listener.listen(new UserEvent("SOMETHING", "test@mail.com"));

        verifyNoInteractions(emailService);
    }

    @Test
    void testMissingEmail() {
        EmailService emailService = mock(EmailService.class);
        NotificationListener listener = new NotificationListener(emailService);

        listener.listen(new UserEvent("CREATED", null));

        verifyNoInteractions(emailService);
    }
}
