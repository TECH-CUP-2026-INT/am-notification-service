package co.edu.escuelaing.techcup.notifications.listener;

import co.edu.escuelaing.techcup.notifications.dto.event.PlayerConductSanctionedEvent;
import co.edu.escuelaing.techcup.notifications.entity.enums.NotificationType;
import co.edu.escuelaing.techcup.notifications.service.CreateNotificationCommand;
import co.edu.escuelaing.techcup.notifications.service.NotificationService;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ConductSanctionEventListenerImpl implements ConductSanctionEventListener {

    private final NotificationService notificationService;

    public ConductSanctionEventListenerImpl(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void onPlayerConductSanctioned(PlayerConductSanctionedEvent event) {
        String matchesLabel = event.matchesSuspended() == 1 ? "1 partido" : event.matchesSuspended() + " partidos";
        String message = "El Organizador te sancionó por conducta: quedas suspendido para "
                + matchesLabel + ". Motivo: " + event.reason();

        // TODO: mismo supuesto que en SanctionEventListenerImpl — playerId de Torneos se
        // asume igual al userId de la cuenta de plataforma hasta que se confirme lo contrario.
        // No hay un id de recurso natural (no está ligada a un partido) para referenceId.
        notificationService.create(new CreateNotificationCommand(
                UUID.fromString(event.playerId()), NotificationType.SANCION_CONDUCTA, message, null));
    }
}
