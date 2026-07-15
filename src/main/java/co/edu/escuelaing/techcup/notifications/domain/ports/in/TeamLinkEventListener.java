package co.edu.escuelaing.techcup.notifications.domain.ports.in;

import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamInvitationEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamLinkRequestedEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamLinkRespondedEvent;

/** Puerto de entrada para los eventos de vinculación a equipo del Servicio de Equipos. */
public interface TeamLinkEventListener {

    void onLinkRequested(TeamLinkRequestedEvent event);

    void onLinkResponded(TeamLinkRespondedEvent event);

    void onTeamInvited(TeamInvitationEvent event);
}
