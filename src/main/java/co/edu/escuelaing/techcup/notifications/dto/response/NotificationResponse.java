package co.edu.escuelaing.techcup.notifications.dto.response;

import co.edu.escuelaing.techcup.notifications.entity.enums.NotificationType;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String message,
        UUID referenceId,
        boolean read,
        Instant createdAt,
        Instant readAt
) {
}
