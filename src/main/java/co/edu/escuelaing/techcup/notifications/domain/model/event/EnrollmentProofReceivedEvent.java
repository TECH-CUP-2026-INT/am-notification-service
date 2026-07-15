package co.edu.escuelaing.techcup.notifications.domain.model.event;

import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Inscripción.
 * Marca la inscripción como recibida en estado "pendiente" mientras el organizador la
 * revisa (RF8).
 */
public record EnrollmentProofReceivedEvent(
        UUID enrollmentId,
        UUID teamId,
        UUID recipientId,
        String proofUrl,
        Instant receivedAt
) {
}
