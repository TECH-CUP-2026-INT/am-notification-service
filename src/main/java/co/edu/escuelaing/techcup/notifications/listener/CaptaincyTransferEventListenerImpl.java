package co.edu.escuelaing.techcup.notifications.listener;

import co.edu.escuelaing.techcup.notifications.dto.event.CaptaincyTransferEvent;
import co.edu.escuelaing.techcup.notifications.dto.event.CaptaincyTransferInitiator;
import co.edu.escuelaing.techcup.notifications.entity.enums.NotificationType;
import co.edu.escuelaing.techcup.notifications.service.CreateNotificationCommand;
import co.edu.escuelaing.techcup.notifications.service.NotificationService;
import org.springframework.stereotype.Component;

@Component
public class CaptaincyTransferEventListenerImpl implements CaptaincyTransferEventListener {

    private final NotificationService notificationService;

    public CaptaincyTransferEventListenerImpl(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void onCaptaincyTransferred(CaptaincyTransferEvent event) {
        if (event.initiatedBy() == CaptaincyTransferInitiator.DELEGATION) {
            String message = "El Capitán del equipo " + event.teamName() + " te delegó el rol de Capitán.";
            notificationService.create(new CreateNotificationCommand(
                    event.newCaptainId(), NotificationType.CAPITANIA_CEDIDA, message, event.teamId()));
        } else {
            String message = "Un jugador aplicó para ser el nuevo Capitán del equipo " + event.teamName() + ".";
            notificationService.create(new CreateNotificationCommand(
                    event.currentCaptainId(), NotificationType.CAPITANIA_SOLICITADA, message, event.teamId()));
        }
    }
}
