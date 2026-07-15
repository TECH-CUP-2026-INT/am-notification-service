package co.edu.escuelaing.techcup.notifications.domain.service;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.CaptaincyTransferEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.CaptaincyTransferInitiator;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CreateNotificationCommand;
import org.springframework.stereotype.Service;

/** Traduce un CaptaincyTransferEvent al comando de notificación correspondiente. */
@Service
public class CaptaincyTransferNotificationComposer {

    public CreateNotificationCommand compose(CaptaincyTransferEvent event) {
        if (event.initiatedBy() == CaptaincyTransferInitiator.DELEGATION) {
            String message = "El Capitán del equipo " + event.teamName() + " te delegó el rol de Capitán.";
            return new CreateNotificationCommand(
                    event.newCaptainId(), NotificationType.CAPITANIA_CEDIDA, message, event.teamId());
        }

        String message = "Un jugador aplicó para ser el nuevo Capitán del equipo " + event.teamName() + ".";
        return new CreateNotificationCommand(
                event.currentCaptainId(), NotificationType.CAPITANIA_SOLICITADA, message, event.teamId());
    }
}
