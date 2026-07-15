package co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.producer;

import co.edu.escuelaing.techcup.notifications.domain.model.Notification;
import co.edu.escuelaing.techcup.notifications.domain.ports.out.NotificationEventPublisherPort;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.MessagingProperties;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.dto.NotificationCreatedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica hacia el exchange compartido de CloudAMQP (techcup.exchange), desacoplado del
 * modelo interno (ver NotificationCreatedMessage), para que el servicio de Estadísticas
 * u otro consumidor futuro pueda suscribirse.
 *
 * <p>La routing key {@code techcup.notification.event.created} es una estimación —
 * sigue el mismo patrón {@code techcup.<dominio>.event.*} usado por Competencia
 * ({@code techcup.match.event.*}) y Torneos ({@code techcup.tournament.event.*}) — y
 * está pendiente de confirmar con el equipo de Estadísticas.
 */
@Component
@RequiredArgsConstructor
public class NotificationEventPublisher implements NotificationEventPublisherPort {

    private static final String NOTIFICATION_CREATED_ROUTING_KEY = "techcup.notification.event.created";

    private final AmqpTemplate amqpTemplate;
    private final MessagingProperties messagingProperties;

    @Override
    public void publishNotificationCreated(Notification notification) {
        NotificationCreatedMessage message = new NotificationCreatedMessage(
                1,
                notification.getId(),
                notification.getRecipientId(),
                notification.getType(),
                notification.getCreatedAt());
        amqpTemplate.convertAndSend(messagingProperties.sharedExchange(), NOTIFICATION_CREATED_ROUTING_KEY, message);
    }
}
