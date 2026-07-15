package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.controller.events;

import co.edu.escuelaing.techcup.notifications.domain.ports.in.CaptaincyTransferEventListener;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.TeamLinkEventListener;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.CaptaincyTransferRequest;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.TeamInvitationRequest;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.TeamLinkRequestedRequest;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.TeamLinkRespondedRequest;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.swagger.TeamEventApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Webhooks propuestos para el Servicio de Equipos. Contratos pendientes de confirmar:
 * ver TeamLinkRequestedRequest, TeamLinkRespondedRequest, TeamInvitationRequest,
 * CaptaincyTransferRequest. Segunda puerta de entrada equivalente:
 * infrastructure/out/messaging/consumer (RabbitMQ).
 */
@RestController
@RequestMapping("/api/notificaciones/equipos")
@RequiredArgsConstructor
public class TeamEventController implements TeamEventApi {

    private final TeamLinkEventListener teamLinkEventListener;
    private final CaptaincyTransferEventListener captaincyTransferEventListener;

    @Override
    @PostMapping("/solicitudes")
    public ResponseEntity<Void> receiveLinkRequest(@RequestBody @Valid TeamLinkRequestedRequest request) {
        teamLinkEventListener.onLinkRequested(request.toDomain());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Override
    @PostMapping("/respuestas")
    public ResponseEntity<Void> receiveLinkResponse(@RequestBody @Valid TeamLinkRespondedRequest request) {
        teamLinkEventListener.onLinkResponded(request.toDomain());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Override
    @PostMapping("/invitaciones")
    public ResponseEntity<Void> receiveInvitation(@RequestBody @Valid TeamInvitationRequest request) {
        teamLinkEventListener.onTeamInvited(request.toDomain());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Override
    @PostMapping("/capitania")
    public ResponseEntity<Void> receiveCaptaincyTransfer(@RequestBody @Valid CaptaincyTransferRequest request) {
        captaincyTransferEventListener.onCaptaincyTransferred(request.toDomain());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
