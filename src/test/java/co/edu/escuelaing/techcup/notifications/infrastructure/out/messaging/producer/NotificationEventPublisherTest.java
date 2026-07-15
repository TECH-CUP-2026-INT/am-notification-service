package co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.producer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import co.edu.escuelaing.techcup.notifications.domain.model.Notification;
import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.MessagingProperties;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.dto.NotificationCreatedMessage;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;

@ExtendWith(MockitoExtension.class)
class NotificationEventPublisherTest {

    @Mock
    private AmqpTemplate amqpTemplate;

    @Test
    void publishNotificationCreated_sendsMinimalPayloadToSharedExchange() {
        MessagingProperties messagingProperties = new MessagingProperties(
                "notificaciones.eventos", "notificaciones.eventos.dlx", "techcup.exchange");
        NotificationEventPublisher publisher = new NotificationEventPublisher(amqpTemplate, messagingProperties);

        Notification notification = new Notification();
        notification.setRecipientId(UUID.randomUUID());
        notification.setType(NotificationType.SANCION_TARJETAS);
        notification.setCreatedAt(Instant.now());

        publisher.publishNotificationCreated(notification);

        ArgumentCaptor<NotificationCreatedMessage> messageCaptor = ArgumentCaptor.forClass(NotificationCreatedMessage.class);
        verify(amqpTemplate).convertAndSend(
                eq("techcup.exchange"), eq("techcup.notification.event.created"), messageCaptor.capture());

        NotificationCreatedMessage message = messageCaptor.getValue();
        assertThat(message.notificationId()).isEqualTo(notification.getId());
        assertThat(message.recipientId()).isEqualTo(notification.getRecipientId());
        assertThat(message.type()).isEqualTo(NotificationType.SANCION_TARJETAS);
        assertThat(message.createdAt()).isEqualTo(notification.getCreatedAt());
    }
}
