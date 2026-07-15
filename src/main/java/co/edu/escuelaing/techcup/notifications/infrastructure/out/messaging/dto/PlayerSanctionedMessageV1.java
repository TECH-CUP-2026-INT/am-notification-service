package co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.dto;

import co.edu.escuelaing.techcup.notifications.domain.model.event.CardType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.PlayerSanctionedEvent;
import java.time.Instant;
import java.util.UUID;

/** Payload versionado de la cola notificaciones.sanciones.q. */
public record PlayerSanctionedMessageV1(
        int schemaVersion,
        UUID matchId,
        UUID teamId,
        UUID playerId,
        CardType triggeringCardType,
        int yellowCardsInMatch,
        Instant occurredAt
) {
    public PlayerSanctionedEvent toDomain() {
        return new PlayerSanctionedEvent(matchId, teamId, playerId, triggeringCardType, yellowCardsInMatch, occurredAt);
    }
}
