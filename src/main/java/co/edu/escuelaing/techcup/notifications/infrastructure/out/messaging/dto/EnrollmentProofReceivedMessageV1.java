package co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.dto;

import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentProofReceivedEvent;
import java.time.Instant;
import java.util.UUID;

/** Payload versionado de la cola notificaciones.inscripciones.comprobante.q. */
public record EnrollmentProofReceivedMessageV1(
        int schemaVersion,
        UUID enrollmentId,
        UUID teamId,
        UUID recipientId,
        String proofUrl,
        Instant receivedAt
) {
    public EnrollmentProofReceivedEvent toDomain() {
        return new EnrollmentProofReceivedEvent(enrollmentId, teamId, recipientId, proofUrl, receivedAt);
    }
}
