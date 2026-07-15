package co.edu.escuelaing.techcup.notifications.domain.service;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamInvitationEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamLinkRequestedEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamLinkRespondedEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CreateNotificationCommand;
import org.springframework.stereotype.Service;

/** Traduce los eventos de vinculación/invitación a equipo al comando de notificación correspondiente. */
@Service
public class TeamLinkNotificationComposer {

    public CreateNotificationCommand composeLinkRequested(TeamLinkRequestedEvent event) {
        String message = event.requesterName() + " solicitó vincularse al equipo " + event.teamName() + ".";

        return new CreateNotificationCommand(
                event.recipientId(), NotificationType.SOLICITUD_VINCULACION_EQUIPO, message, event.requestId());
    }

    public CreateNotificationCommand composeLinkResponded(TeamLinkRespondedEvent event) {
        NotificationType type = event.accepted()
                ? NotificationType.VINCULACION_ACEPTADA
                : NotificationType.VINCULACION_RECHAZADA;
        String message = event.accepted()
                ? "Tu solicitud para unirte al equipo " + event.teamName() + " fue aceptada."
                : "Tu solicitud para unirte al equipo " + event.teamName() + " fue rechazada.";

        return new CreateNotificationCommand(event.recipientId(), type, message, event.teamId());
    }

    public CreateNotificationCommand composeTeamInvited(TeamInvitationEvent event) {
        String message = event.invitedBy() + " te invitó a unirte al equipo " + event.teamName() + ".";

        return new CreateNotificationCommand(
                event.invitedUserId(), NotificationType.INVITACION_EQUIPO, message, event.invitationId());
    }
}
