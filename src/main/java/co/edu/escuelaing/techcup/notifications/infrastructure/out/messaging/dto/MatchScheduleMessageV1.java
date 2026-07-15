package co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.dto;

import co.edu.escuelaing.techcup.notifications.domain.model.event.MatchScheduleAction;
import co.edu.escuelaing.techcup.notifications.domain.model.event.MatchScheduleEvent;
import java.time.Instant;
import java.util.UUID;

/** Payload versionado de la cola notificaciones.partidos.q. */
public record MatchScheduleMessageV1(
        int schemaVersion,
        UUID matchId,
        UUID teamHomeId,
        UUID teamAwayId,
        UUID recipientId,
        MatchScheduleAction action,
        Instant scheduledAt,
        Instant previousScheduledAt,
        Instant occurredAt
) {
    public MatchScheduleEvent toDomain() {
        return new MatchScheduleEvent(
                matchId, teamHomeId, teamAwayId, recipientId, action, scheduledAt, previousScheduledAt, occurredAt);
    }
}
