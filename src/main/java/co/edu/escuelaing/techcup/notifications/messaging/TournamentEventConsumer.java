package co.edu.escuelaing.techcup.notifications.messaging;

import co.edu.escuelaing.techcup.notifications.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume {@code techcup.tournament.event.*} desde la cola propia de Notificaciones.
 * Por ahora solo loguea — ver MatchEventConsumer / RabbitMQConfig para el motivo.
 */
@Component
public class TournamentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TournamentEventConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.TOURNAMENT_EVENTS_QUEUE)
    public void onTournamentEvent(TournamentFinalizedEvent event) {
        log.info("Evento de torneo recibido: torneo={}, ocurrido={}", event.tournamentId(), event.occurredAt());
    }
}
