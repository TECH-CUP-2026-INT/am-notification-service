package co.edu.escuelaing.techcup.notifications.listener;

import co.edu.escuelaing.techcup.notifications.dto.event.TeamInvitationEvent;
import co.edu.escuelaing.techcup.notifications.dto.event.TeamLinkRequestEvent;
import co.edu.escuelaing.techcup.notifications.dto.event.TeamLinkResponseEvent;
import co.edu.escuelaing.techcup.notifications.entity.enums.NotificationType;
import co.edu.escuelaing.techcup.notifications.service.CreateNotificationCommand;
import co.edu.escuelaing.techcup.notifications.service.NotificationService;
import org.springframework.stereotype.Component;

@Component
public class TeamLinkEventListenerImpl implements TeamLinkEventListener {

    private final NotificationService notificationService;

    public TeamLinkEventListenerImpl(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void onLinkRequested(TeamLinkRequestEvent event) {
        String message = event.requesterName() + " solicitó vincularse al equipo " + event.teamName() + ".";

        notificationService.create(new CreateNotificationCommand(
                event.recipientId(), NotificationType.SOLICITUD_VINCULACION_EQUIPO, message, event.requestId()));
    }

    @Override
    public void onLinkResponded(TeamLinkResponseEvent event) {
        NotificationType type = event.accepted()
                ? NotificationType.VINCULACION_ACEPTADA
                : NotificationType.VINCULACION_RECHAZADA;
        String message = event.accepted()
                ? "Tu solicitud para unirte al equipo " + event.teamName() + " fue aceptada."
                : "Tu solicitud para unirte al equipo " + event.teamName() + " fue rechazada.";

        notificationService.create(new CreateNotificationCommand(event.recipientId(), type, message, event.teamId()));
    }

    @Override
    public void onTeamInvited(TeamInvitationEvent event) {
        String message = event.invitedBy() + " te invitó a unirte al equipo " + event.teamName() + ".";

        notificationService.create(new CreateNotificationCommand(
                event.invitedUserId(), NotificationType.INVITACION_EQUIPO, message, event.invitationId()));
    }
}
