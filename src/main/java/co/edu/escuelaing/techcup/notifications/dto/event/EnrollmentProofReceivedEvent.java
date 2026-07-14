package co.edu.escuelaing.techcup.notifications.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Inscripción.
 * Endpoint: POST /api/notificaciones/inscripciones/comprobante. Marca la inscripción
 * como recibida en estado "pendiente" mientras el organizador la revisa (RF8).
 */
public record EnrollmentProofReceivedEvent(
        @NotNull UUID enrollmentId,
        @NotNull UUID teamId,
        @NotNull UUID recipientId,
        @NotBlank String proofUrl,
        @NotNull Instant receivedAt
) {
}
