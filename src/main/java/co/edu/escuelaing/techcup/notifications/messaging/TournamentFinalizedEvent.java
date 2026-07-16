package co.edu.escuelaing.techcup.notifications.messaging;

import java.time.LocalDateTime;

/**
 * Espejo de {@code TournamentFinalizedEvent} tal como lo publica Torneos
 * (RabbitTournamentEventPublisherAdapter, routing key
 * {@code techcup.tournament.event.finalized}).
 */
public record TournamentFinalizedEvent(String tournamentId, LocalDateTime occurredAt) {
}
