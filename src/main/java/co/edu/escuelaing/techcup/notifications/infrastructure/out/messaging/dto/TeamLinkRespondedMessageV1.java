package co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.dto;

import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamLinkRespondedEvent;
import java.time.Instant;
import java.util.UUID;

/** Payload versionado de la cola notificaciones.equipos.respuestas.q. */
public record TeamLinkRespondedMessageV1(
        int schemaVersion,
        UUID teamId,
        String teamName,
        UUID requestId,
        UUID recipientId,
        boolean accepted,
        Instant respondedAt
) {
    public TeamLinkRespondedEvent toDomain() {
        return new TeamLinkRespondedEvent(teamId, teamName, requestId, recipientId, accepted, respondedAt);
    }
}
