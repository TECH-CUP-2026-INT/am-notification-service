package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request;

import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentProofReceivedEvent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Inscripción.
 * Endpoint: POST /api/notificaciones/inscripciones/comprobante. Marca la inscripción
 * como recibida en estado "pendiente" mientras el organizador la revisa (RF8).
 */
public record EnrollmentProofReceivedRequest(
        @NotNull UUID enrollmentId,
        @NotNull UUID teamId,
        @NotNull UUID recipientId,
        @NotBlank @Size(max = 2048) String proofUrl,
        @NotNull Instant receivedAt
) {
    public EnrollmentProofReceivedEvent toDomain() {
        return new EnrollmentProofReceivedEvent(enrollmentId, teamId, recipientId, proofUrl, receivedAt);
    }
}
