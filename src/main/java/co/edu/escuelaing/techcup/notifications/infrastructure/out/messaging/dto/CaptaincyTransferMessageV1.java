package co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.dto;

import co.edu.escuelaing.techcup.notifications.domain.model.event.CaptaincyTransferEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.CaptaincyTransferInitiator;
import java.time.Instant;
import java.util.UUID;

/** Payload versionado de la cola notificaciones.equipos.capitania.q. */
public record CaptaincyTransferMessageV1(
        int schemaVersion,
        UUID teamId,
        String teamName,
        UUID currentCaptainId,
        UUID newCaptainId,
        CaptaincyTransferInitiator initiatedBy,
        Instant occurredAt
) {
    public CaptaincyTransferEvent toDomain() {
        return new CaptaincyTransferEvent(teamId, teamName, currentCaptainId, newCaptainId, initiatedBy, occurredAt);
    }
}
