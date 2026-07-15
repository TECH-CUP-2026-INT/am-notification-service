package co.edu.escuelaing.techcup.notifications.domain.model.event;

import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Inscripción.
 * {@code reason} es opcional (por ejemplo, motivo de rechazo).
 */
public record EnrollmentStatusChangedEvent(
        UUID enrollmentId,
        UUID teamId,
        UUID recipientId,
        EnrollmentStatus newStatus,
        String reason,
        Instant occurredAt
) {
}
