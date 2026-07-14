package co.edu.escuelaing.techcup.notifications.listener;

import co.edu.escuelaing.techcup.notifications.dto.event.TeamInvitationEvent;
import co.edu.escuelaing.techcup.notifications.dto.event.TeamLinkRequestEvent;
import co.edu.escuelaing.techcup.notifications.dto.event.TeamLinkResponseEvent;

/** Puerto de entrada para los eventos de vinculación a equipo del Servicio de Equipos. */
public interface TeamLinkEventListener {

    void onLinkRequested(TeamLinkRequestEvent event);

    void onLinkResponded(TeamLinkResponseEvent event);

    void onTeamInvited(TeamInvitationEvent event);
}
