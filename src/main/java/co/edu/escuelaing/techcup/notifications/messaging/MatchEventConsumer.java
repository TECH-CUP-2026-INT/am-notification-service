package co.edu.escuelaing.techcup.notifications.messaging;

import co.edu.escuelaing.techcup.notifications.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume {@code techcup.match.event.*} desde la cola propia de Notificaciones. Por
 * ahora solo loguea — el payload no trae destinatario ni mensaje, así que todavía no
 * hay un criterio para traducirlo a una notificación (ver RabbitMQConfig).
 */
@Component
public class MatchEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(MatchEventConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.MATCH_EVENTS_QUEUE)
    public void onMatchEvent(MatchStatEvent event) {
        log.info("Evento de partido recibido: jugador={}, partido={}, torneo={}, goles={}, amarillas={}, rojas={}",
                event.playerId(), event.matchId(), event.tournamentId(),
                event.goals(), event.yellowCards(), event.redCards());
    }
}
