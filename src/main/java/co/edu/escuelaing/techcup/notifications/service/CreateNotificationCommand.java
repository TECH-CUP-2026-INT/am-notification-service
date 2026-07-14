package co.edu.escuelaing.techcup.notifications.service;

import co.edu.escuelaing.techcup.notifications.entity.enums.NotificationType;
import java.util.UUID;

/**
 * Frontera entre los listeners (que interpretan eventos de otros servicios) y la
 * persistencia. Un listener nunca toca el repositorio directamente: arma este comando
 * a partir del evento externo y se lo entrega a NotificationService.
 */
public record CreateNotificationCommand(
        UUID recipientId,
        NotificationType type,
        String message,
        UUID referenceId
) {
}
