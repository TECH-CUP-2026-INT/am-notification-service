package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.response;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
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
