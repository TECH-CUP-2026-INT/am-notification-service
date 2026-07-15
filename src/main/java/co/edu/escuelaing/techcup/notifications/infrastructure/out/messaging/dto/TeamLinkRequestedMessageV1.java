package co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.dto;

import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamLinkRequestedEvent;
import java.time.Instant;
import java.util.UUID;

/** Payload versionado de la cola notificaciones.equipos.solicitudes.q. */
public record TeamLinkRequestedMessageV1(
        int schemaVersion,
        UUID teamId,
        String teamName,
        UUID requesterId,
        String requesterName,
        UUID recipientId,
        UUID requestId,
        Instant occurredAt
) {
    public TeamLinkRequestedEvent toDomain() {
        return new TeamLinkRequestedEvent(teamId, teamName, requesterId, requesterName, recipientId, requestId, occurredAt);
    }
}
