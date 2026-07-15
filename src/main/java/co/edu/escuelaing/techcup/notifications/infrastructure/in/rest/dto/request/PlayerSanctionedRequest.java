package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request;

import co.edu.escuelaing.techcup.notifications.domain.model.event.CardType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.PlayerSanctionedEvent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO CONFIRMADO con el Servicio de Partidos: espejo exacto de
 * PlayerSanctionedPayload, publicado hoy por RestSanctionNotifier a
 * POST /api/notificaciones/sanciones.
 *
 * <p>Pendiente de confirmar con el equipo de Partidos: {@code playerId} es el id del
 * jugador en su dominio, no necesariamente el {@code userId} de la cuenta de la
 * plataforma. Hasta que se confirme lo contrario, este servicio asume que son el mismo
 * UUID y lo usa directamente como destinatario (ver SanctionEventListenerImpl).
 */
public record PlayerSanctionedRequest(
        @NotNull UUID matchId,
        @NotNull UUID teamId,
        @NotNull UUID playerId,
        @NotNull CardType triggeringCardType,
        @PositiveOrZero int yellowCardsInMatch,
        @NotNull Instant occurredAt
) {
    public PlayerSanctionedEvent toDomain() {
        return new PlayerSanctionedEvent(matchId, teamId, playerId, triggeringCardType, yellowCardsInMatch, occurredAt);
    }
}
