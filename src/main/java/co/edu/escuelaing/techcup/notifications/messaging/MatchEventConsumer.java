package co.edu.escuelaing.techcup.notifications.messaging;

import co.edu.escuelaing.techcup.notifications.config.RabbitMQConfig;
import co.edu.escuelaing.techcup.notifications.entity.enums.NotificationType;
import co.edu.escuelaing.techcup.notifications.service.CreateNotificationCommand;
import co.edu.escuelaing.techcup.notifications.service.NotificationService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume {@code techcup.match.event.*} desde la cola propia de Notificaciones y le avisa
 * al jugador el resultado de su partido. {@code playerId} es el único campo del evento
 * que sirve como destinatario (a diferencia de TournamentFinalizedEvent, que no trae
 * ninguno — ver TournamentEventConsumer, que por eso sigue siendo solo un log).
 *
 * <p>TODO: mismo supuesto que en SanctionEventListenerImpl — playerId se asume igual al
 * userId de la cuenta de plataforma hasta que se confirme lo contrario con Competencia.
 */
@Component
public class MatchEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(MatchEventConsumer.class);

    private final NotificationService notificationService;

    public MatchEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.MATCH_EVENTS_QUEUE)
    public void onMatchEvent(MatchStatEvent event) {
        try {
            String message = buildMessage(event);
            notificationService.create(new CreateNotificationCommand(
                    UUID.fromString(event.playerId()), NotificationType.RESULTADO_PARTIDO, message,
                    UUID.fromString(event.matchId())));
        } catch (Exception ex) {
            log.warn("No fue posible procesar el evento de partido del jugador {}: {}",
                    event.playerId(), ex.getMessage());
        }
    }

    private String buildMessage(MatchStatEvent event) {
        String resultText = switch (event.result() == null ? "" : event.result()) {
            case "WON" -> "ganó";
            case "LOST" -> "perdió";
            case "DRAWN" -> "empató";
            default -> "jugó";
        };

        StringBuilder message = new StringBuilder("Tu equipo ").append(resultText).append(" el partido.");
        if (event.goals() != null && event.goals() > 0) {
            message.append(" Anotaste ").append(event.goals()).append(event.goals() == 1 ? " gol." : " goles.");
        }
        if (event.redCards() != null && event.redCards() > 0) {
            message.append(" Recibiste tarjeta roja.");
        } else if (event.yellowCards() != null && event.yellowCards() > 0) {
            message.append(" Recibiste tarjeta amarilla.");
        }
        return message.toString();
    }
}
