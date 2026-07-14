package co.edu.escuelaing.techcup.notifications.dto.event;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Inscripción.
 * Endpoint: POST /api/notificaciones/inscripciones/estado. {@code reason} es opcional
 * (por ejemplo, motivo de rechazo).
 */
public record EnrollmentStatusChangedEvent(
        @NotNull UUID enrollmentId,
        @NotNull UUID teamId,
        @NotNull UUID recipientId,
        @NotNull EnrollmentStatus newStatus,
        String reason,
        @NotNull Instant occurredAt
) {
}
