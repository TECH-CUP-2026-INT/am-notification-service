package co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.dto;

import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamInvitationEvent;
import java.time.Instant;
import java.util.UUID;

/** Payload versionado de la cola notificaciones.equipos.invitaciones.q. */
public record TeamInvitationMessageV1(
        int schemaVersion,
        UUID teamId,
        String teamName,
        UUID invitedUserId,
        UUID invitationId,
        String invitedBy,
        Instant occurredAt
) {
    public TeamInvitationEvent toDomain() {
        return new TeamInvitationEvent(teamId, teamName, invitedUserId, invitationId, invitedBy, occurredAt);
    }
}
