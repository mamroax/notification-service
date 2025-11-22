package com.example.notification.api;

import com.example.notification.dto.UserOperationMessage;
import com.example.notification.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class NotificationControllerTest {

    @Test
    void testSendCreated() throws Exception {
        EmailService emailService = mock(EmailService.class);
        NotificationController controller = new NotificationController(emailService);

        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"operation":"CREATED","email":"test@mail.com"}
                        """))
                .andExpect(status().isOk())
                .andExpect(content().string("Sent"));

        verify(emailService).sendSimple(
                eq("test@mail.com"),
                eq("Аккаунт создан"),
                anyString()
        );
    }

    @Test
    void testSendDeleted() throws Exception {
        EmailService emailService = mock(EmailService.class);
        NotificationController controller = new NotificationController(emailService);

        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"operation":"DELETED","email":"test@mail.com"}
                        """))
                .andExpect(status().isOk())
                .andExpect(content().string("Sent"));

        verify(emailService).sendSimple(
                eq("test@mail.com"),
                eq("Аккаунт удалён"),
                anyString()
        );
    }

//    @Test
//    void testSendMissingEmail() throws Exception {
//        EmailService emailService = mock(EmailService.class);
//        NotificationController controller = new NotificationController(emailService);
//        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
//
//        mvc.perform(post("/api/notifications/send")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"operation\":\"CREATED\",\"email\":\"test@mail.com\"}"))
//                .andExpect(status().isBadRequest());
//    }

    @Test
    void testSendUnknownOperation() throws Exception {
        EmailService emailService = mock(EmailService.class);
        NotificationController controller = new NotificationController(emailService);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"operation":"SOMETHING","email":"mail@mail.com"}
                        """))
                .andExpect(status().isBadRequest());
    }
}
