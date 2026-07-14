package co.edu.escuelaing.techcup.notifications.listener;

import co.edu.escuelaing.techcup.notifications.dto.event.CardType;
import co.edu.escuelaing.techcup.notifications.dto.event.PlayerSanctionedEvent;
import co.edu.escuelaing.techcup.notifications.entity.enums.NotificationType;
import co.edu.escuelaing.techcup.notifications.service.CreateNotificationCommand;
import co.edu.escuelaing.techcup.notifications.service.NotificationService;
import org.springframework.stereotype.Component;

@Component
public class SanctionEventListenerImpl implements SanctionEventListener {

    private final NotificationService notificationService;

    public SanctionEventListenerImpl(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void onPlayerSanctioned(PlayerSanctionedEvent event) {
        String message = event.triggeringCardType() == CardType.RED
                ? "Recibiste una tarjeta roja directa: quedas sancionado para el próximo partido."
                : "Acumulaste " + event.yellowCardsInMatch()
                        + " tarjetas amarillas en el partido: quedas sancionado para el próximo partido.";

        // TODO: confirmar con el equipo de Partidos si playerId es el mismo userId de la
        // cuenta de plataforma (asunción actual) o si hay que resolverlo contra otro servicio.
        notificationService.create(new CreateNotificationCommand(
                event.playerId(), NotificationType.SANCION_TARJETAS, message, event.matchId()));
    }
}
