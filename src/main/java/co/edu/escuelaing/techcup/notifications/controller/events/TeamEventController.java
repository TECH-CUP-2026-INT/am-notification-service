package co.edu.escuelaing.techcup.notifications.controller.events;

import co.edu.escuelaing.techcup.notifications.dto.event.TeamInvitationEvent;
import co.edu.escuelaing.techcup.notifications.dto.event.TeamLinkRequestEvent;
import co.edu.escuelaing.techcup.notifications.dto.event.TeamLinkResponseEvent;
import co.edu.escuelaing.techcup.notifications.listener.TeamLinkEventListener;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Webhooks propuestos para el Servicio de Equipos. Contratos pendientes de confirmar:
 * ver TeamLinkRequestEvent, TeamLinkResponseEvent, TeamInvitationEvent.
 */
@RestController
@RequestMapping("/api/notificaciones/equipos")
public class TeamEventController {

    private final TeamLinkEventListener teamLinkEventListener;

    public TeamEventController(TeamLinkEventListener teamLinkEventListener) {
        this.teamLinkEventListener = teamLinkEventListener;
    }

    @PostMapping("/solicitudes")
    public ResponseEntity<Void> receiveLinkRequest(@RequestBody @Valid TeamLinkRequestEvent event) {
        teamLinkEventListener.onLinkRequested(event);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/respuestas")
    public ResponseEntity<Void> receiveLinkResponse(@RequestBody @Valid TeamLinkResponseEvent event) {
        teamLinkEventListener.onLinkResponded(event);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/invitaciones")
    public ResponseEntity<Void> receiveInvitation(@RequestBody @Valid TeamInvitationEvent event) {
        teamLinkEventListener.onTeamInvited(event);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
