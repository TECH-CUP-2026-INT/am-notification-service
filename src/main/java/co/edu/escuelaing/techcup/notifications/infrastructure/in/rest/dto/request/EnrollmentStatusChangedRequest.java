package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request;

import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentStatus;
import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentStatusChangedEvent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Inscripción.
 * Endpoint: POST /api/notificaciones/inscripciones/estado. {@code reason} es opcional
 * (por ejemplo, motivo de rechazo).
 */
public record EnrollmentStatusChangedRequest(
        @NotNull UUID enrollmentId,
        @NotNull UUID teamId,
        @NotNull UUID recipientId,
        @NotNull EnrollmentStatus newStatus,
        @Size(max = 500) String reason,
        @NotNull Instant occurredAt
) {
    public EnrollmentStatusChangedEvent toDomain() {
        return new EnrollmentStatusChangedEvent(enrollmentId, teamId, recipientId, newStatus, reason, occurredAt);
    }
}
