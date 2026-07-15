package co.edu.escuelaing.techcup.notifications.domain.service;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.CardType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.PlayerSanctionedEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CreateNotificationCommand;
import org.springframework.stereotype.Service;

/** Traduce un PlayerSanctionedEvent al comando de notificación correspondiente. */
@Service
public class SanctionNotificationComposer {

    public CreateNotificationCommand compose(PlayerSanctionedEvent event) {
        String message = event.triggeringCardType() == CardType.RED
                ? "Recibiste una tarjeta roja directa: quedas sancionado para el próximo partido."
                : "Acumulaste " + event.yellowCardsInMatch()
                        + " tarjetas amarillas en el partido: quedas sancionado para el próximo partido.";

        // TODO: confirmar con el equipo de Partidos si playerId es el mismo userId de la
        // cuenta de plataforma (asunción actual) o si hay que resolverlo contra otro servicio.
        return new CreateNotificationCommand(event.playerId(), NotificationType.SANCION_TARJETAS, message, event.matchId());
    }
}
