package co.edu.escuelaing.techcup.notifications.domain.model.event;

import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO CONFIRMADO con el Servicio de Partidos: espejo exacto de
 * PlayerSanctionedPayload, publicado hoy por RestSanctionNotifier.
 *
 * <p>Pendiente de confirmar con el equipo de Partidos: {@code playerId} es el id del
 * jugador en su dominio, no necesariamente el {@code userId} de la cuenta de la
 * plataforma. Hasta que se confirme lo contrario, este servicio asume que son el mismo
 * UUID y lo usa directamente como destinatario (ver SanctionEventListenerImpl).
 */
public record PlayerSanctionedEvent(
        UUID matchId,
        UUID teamId,
        UUID playerId,
        CardType triggeringCardType,
        int yellowCardsInMatch,
        Instant occurredAt
) {
}
