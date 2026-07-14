package co.edu.escuelaing.techcup.notifications.listener;

import co.edu.escuelaing.techcup.notifications.dto.event.MatchScheduleEvent;
import co.edu.escuelaing.techcup.notifications.entity.enums.NotificationType;
import co.edu.escuelaing.techcup.notifications.service.CreateNotificationCommand;
import co.edu.escuelaing.techcup.notifications.service.NotificationService;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class MatchScheduleEventListenerImpl implements MatchScheduleEventListener {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("America/Bogota"));

    private final NotificationService notificationService;

    public MatchScheduleEventListenerImpl(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void onScheduleChanged(MatchScheduleEvent event) {
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

        notificationService.create(new CreateNotificationCommand(event.recipientId(), type, message, event.matchId()));
    }
}
