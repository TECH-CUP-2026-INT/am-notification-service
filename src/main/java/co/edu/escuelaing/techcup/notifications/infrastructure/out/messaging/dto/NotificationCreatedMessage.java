package co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.dto;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import java.time.Instant;
import java.util.UUID;

/**
 * Payload publicado hacia el exchange de Estadísticas tras crear una notificación.
 * Desacoplado a propósito: nunca se envía la entidad completa (ni el mensaje de texto),
 * solo lo que un consumidor externo necesita para agregar métricas.
 */
public record NotificationCreatedMessage(
        int schemaVersion,
        UUID notificationId,
        UUID recipientId,
        NotificationType type,
        Instant createdAt
) {
}
