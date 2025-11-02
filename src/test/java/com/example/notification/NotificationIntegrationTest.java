package com.example.notification;

import com.example.notification.dto.UserOperationMessage;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = { "user-operations" })
class NotificationIntegrationTest {

    @Autowired
    EmbeddedKafkaBroker embeddedKafka;

    static GreenMail greenMail;

    static KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    TestRestTemplate restTemplate;

    @BeforeAll
    static void beforeAll() {
        greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.start();
    }

    @AfterAll
    static void afterAll() {
        greenMail.stop();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        // Point mail to GreenMail and Kafka will be configured later in the test setup
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> ServerSetupTest.SMTP.getPort());
    }

    @BeforeEach
    void setup() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.springframework.kafka.support.serializer.JsonSerializer.class);
        DefaultKafkaProducerFactory<String, Object> pf = new DefaultKafkaProducerFactory<>(props);
        kafkaTemplate = new KafkaTemplate<>(pf);
    }

    @Test
    void whenKafkaReceivesCreated_thenEmailIsSent() throws Exception {
        UserOperationMessage msg = new UserOperationMessage("CREATED", "user@example.com");
        kafkaTemplate.send("user-operations", msg);
        kafkaTemplate.flush();

        // Wait for mail to arrive
        boolean received = greenMail.waitForIncomingEmail(5000, 1);
        assertThat(received).isTrue();
        MimeMessage[] msgs = greenMail.getReceivedMessages();
        assertThat(msgs).hasSize(1);
        assertThat(msgs[0].getSubject()).isEqualTo("Аккаунт создан");
        String body = (String) msgs[0].getContent();
        assertThat(body).contains("Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.");
    }

    @Test
    void whenKafkaReceivesDeleted_thenEmailIsSent() throws Exception {
        UserOperationMessage msg = new UserOperationMessage("DELETED", "deleteme@example.com");
        kafkaTemplate.send("user-operations", msg);
        kafkaTemplate.flush();

        boolean received = greenMail.waitForIncomingEmail(5000, 1);
        assertThat(received).isTrue();
        MimeMessage[] msgs = greenMail.getReceivedMessages();
        assertThat(msgs[0].getSubject()).isEqualTo("Аккаунт удалён");
        String body = (String) msgs[0].getContent();
        assertThat(body).contains("Здравствуйте! Ваш аккаунт был удалён.");
    }

    @Test
    void restEndpointSendsEmail() throws Exception {
        UserOperationMessage msg = new UserOperationMessage("CREATED", "rest@example.com");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserOperationMessage> req = new HttpEntity<>(msg, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity("/api/notifications/send", req, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

        boolean received = greenMail.waitForIncomingEmail(5000, 1);
        assertThat(received).isTrue();
        MimeMessage[] msgs = greenMail.getReceivedMessages();
        assertThat(msgs[0].getSubject()).isEqualTo("Аккаунт создан");
    }
}
