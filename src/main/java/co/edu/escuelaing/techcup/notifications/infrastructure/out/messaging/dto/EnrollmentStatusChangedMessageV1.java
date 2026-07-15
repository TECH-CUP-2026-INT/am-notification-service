package co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.dto;

import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentStatus;
import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentStatusChangedEvent;
import java.time.Instant;
import java.util.UUID;

/** Payload versionado de la cola notificaciones.inscripciones.estado.q. */
public record EnrollmentStatusChangedMessageV1(
        int schemaVersion,
        UUID enrollmentId,
        UUID teamId,
        UUID recipientId,
        EnrollmentStatus newStatus,
        String reason,
        Instant occurredAt
) {
    public EnrollmentStatusChangedEvent toDomain() {
        return new EnrollmentStatusChangedEvent(enrollmentId, teamId, recipientId, newStatus, reason, occurredAt);
    }
}
