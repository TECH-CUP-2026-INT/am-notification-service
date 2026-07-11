package co.edu.escuelaing.techcup.notifications.controller.events;

import co.edu.escuelaing.techcup.notifications.dto.event.MatchScheduleEvent;
import co.edu.escuelaing.techcup.notifications.listener.MatchScheduleEventListener;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Webhook propuesto para el Servicio de Agendamiento. Contrato pendiente de confirmar:
 * ver MatchScheduleEvent.
 */
@RestController
@RequestMapping("/api/notificaciones/partidos")
public class MatchScheduleEventController {

    private final MatchScheduleEventListener matchScheduleEventListener;

    public MatchScheduleEventController(MatchScheduleEventListener matchScheduleEventListener) {
        this.matchScheduleEventListener = matchScheduleEventListener;
    }

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody @Valid MatchScheduleEvent event) {
        matchScheduleEventListener.onScheduleChanged(event);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
