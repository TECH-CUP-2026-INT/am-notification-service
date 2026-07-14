package co.edu.escuelaing.techcup.notifications.controller.events;

import co.edu.escuelaing.techcup.notifications.dto.event.PlayerSanctionedEvent;
import co.edu.escuelaing.techcup.notifications.listener.SanctionEventListener;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Webhook consumido por el Servicio de Partidos (RestSanctionNotifier). Contrato
 * confirmado: ver PlayerSanctionedEvent.
 */
@RestController
@RequestMapping("/api/notificaciones/sanciones")
public class SanctionEventController {

    private final SanctionEventListener sanctionEventListener;

    public SanctionEventController(SanctionEventListener sanctionEventListener) {
        this.sanctionEventListener = sanctionEventListener;
    }

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody @Valid PlayerSanctionedEvent event) {
        sanctionEventListener.onPlayerSanctioned(event);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
