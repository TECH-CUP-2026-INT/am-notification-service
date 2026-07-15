package co.edu.escuelaing.techcup.notifications.domain.service;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.MatchScheduleEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CreateNotificationCommand;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

/** Traduce un MatchScheduleEvent al comando de notificación correspondiente. */
@Service
public class MatchScheduleNotificationComposer {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("America/Bogota"));

    public CreateNotificationCommand compose(MatchScheduleEvent event) {
        NotificationType type;
        String message;

        switch (event.action()) {
            case PROGRAMADO -> {
                type = NotificationType.PARTIDO_PROGRAMADO;
                message = "Tu partido fue programado para el " + DATE_FORMATTER.format(event.scheduledAt()) + ".";
            }
            case REPROGRAMADO -> {
                type = NotificationType.PARTIDO_REPROGRAMADO;
                message = "Tu partido fue reprogramado del "
                        + DATE_FORMATTER.format(event.previousScheduledAt()) + " al "
                        + DATE_FORMATTER.format(event.scheduledAt()) + ".";
            }
            default -> {
                type = NotificationType.PARTIDO_CANCELADO;
                message = "Tu partido programado fue cancelado.";
            }
        }

        return new CreateNotificationCommand(event.recipientId(), type, message, event.matchId());
    }
}
