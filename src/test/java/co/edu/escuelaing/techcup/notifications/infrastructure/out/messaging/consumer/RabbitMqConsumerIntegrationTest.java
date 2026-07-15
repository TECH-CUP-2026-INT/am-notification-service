package co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import co.edu.escuelaing.techcup.notifications.domain.model.event.CardType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.PlayerSanctionedEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.SanctionEventListener;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.MessagingProperties;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.config.RabbitMqConfig;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.dto.PlayerSanctionedMessageV1;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Prueba end-to-end del transporte RabbitMQ (segunda puerta de entrada equivalente a los
 * webhooks REST): un mensaje válido llega al mismo puerto de dominio que hoy invoca
 * SanctionEventController; un mensaje corrupto agota los 3 reintentos y termina en la DLQ.
 * Representa el patrón común a los 9 consumers (todos siguen la misma forma).
 */
@Testcontainers
@SpringBootTest
class RabbitMqConsumerIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:7");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitContainer = new RabbitMQContainer("rabbitmq:3.13-management");

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MessagingProperties messagingProperties;

    @MockitoBean
    private SanctionEventListener sanctionEventListener;

    @Test
    void validMessage_isConsumedAndDelegatedToDomainPort() {
        PlayerSanctionedMessageV1 message = new PlayerSanctionedMessageV1(
                1, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), CardType.RED, 0, Instant.now());

        // sanciones ahora llega por el exchange compartido techcup.exchange (Competencia),
        // no por el exchange local notificaciones.eventos.
        rabbitTemplate.convertAndSend(messagingProperties.sharedExchange(), "techcup.match.event.sancion", message);

        verify(sanctionEventListener, timeout(5000)).onPlayerSanctioned(any(PlayerSanctionedEvent.class));
    }

    @Test
    void malformedMessage_endsUpInDeadLetterQueueAfterRetries() {
        rabbitTemplate.convertAndSend(
                messagingProperties.sharedExchange(), "techcup.match.event.sancion",
                "esto no es un PlayerSanctionedMessageV1 válido");

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            Object dlqMessage = rabbitTemplate.receiveAndConvert(RabbitMqConfig.QUEUE_SANCIONES + ".dlq");
            assertThat(dlqMessage).isNotNull();
        });
    }
}
