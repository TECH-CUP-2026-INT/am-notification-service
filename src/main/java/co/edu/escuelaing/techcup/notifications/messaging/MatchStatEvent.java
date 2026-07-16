package co.edu.escuelaing.techcup.notifications.messaging;

import java.time.LocalDateTime;

/**
 * Espejo de {@code MatchStatEvent} tal como lo publica (o publicará) Competencia —
 * ver docs/rabbitmq-integration.md del Servicio de Estadísticas, sección "Para
 * astromerge (Competencia)". Routing key: {@code techcup.match.event.*}.
 */
public record MatchStatEvent(
        String playerId,
        String teamId,
        String matchId,
        String tournamentId,
        String result,
        Integer goals,
        Integer yellowCards,
        Integer redCards,
        Integer foulsCommitted,
        Integer minutesPlayed,
        Integer assists,
        Boolean goalkeeper,
        LocalDateTime occurredAt
) {
}
