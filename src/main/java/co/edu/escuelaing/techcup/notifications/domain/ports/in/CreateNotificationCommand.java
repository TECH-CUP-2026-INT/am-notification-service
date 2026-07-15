package co.edu.escuelaing.techcup.notifications.domain.ports.in;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import java.util.UUID;

/**
 * Frontera entre los adaptadores de entrada (REST, RabbitMQ) y el caso de uso de
 * creación de notificación: es el contrato del puerto {@link NotificationUseCase#create},
 * no un detalle de ningún adaptador en particular.
 */
public record CreateNotificationCommand(
        UUID recipientId,
        NotificationType type,
        String message,
        UUID referenceId
) {
}
