package co.edu.escuelaing.techcup.notifications.controller.events;

import co.edu.escuelaing.techcup.notifications.dto.event.PlayerConductSanctionedEvent;
import co.edu.escuelaing.techcup.notifications.listener.ConductSanctionEventListener;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Webhook consumido por el Servicio de Torneos (SanctionNotificationAdapter). Contrato
 * confirmado: ver PlayerConductSanctionedEvent.
 */
@RestController
@RequestMapping("/api/notificaciones/sanciones-conducta")
public class ConductSanctionEventController {

    private final ConductSanctionEventListener conductSanctionEventListener;

    public ConductSanctionEventController(ConductSanctionEventListener conductSanctionEventListener) {
        this.conductSanctionEventListener = conductSanctionEventListener;
    }

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody @Valid PlayerConductSanctionedEvent event) {
        conductSanctionEventListener.onPlayerConductSanctioned(event);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
