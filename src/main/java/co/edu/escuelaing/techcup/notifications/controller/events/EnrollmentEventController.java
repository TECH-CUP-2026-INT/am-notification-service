package co.edu.escuelaing.techcup.notifications.controller.events;

import co.edu.escuelaing.techcup.notifications.dto.event.EnrollmentStatusChangedEvent;
import co.edu.escuelaing.techcup.notifications.listener.EnrollmentEventListener;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Webhooks propuestos para el Servicio de Inscripción. Contrato pendiente de
 * confirmar: ver EnrollmentStatusChangedEvent.
 */
@RestController
@RequestMapping("/api/notificaciones/inscripciones")
public class EnrollmentEventController {

    private final EnrollmentEventListener enrollmentEventListener;

    public EnrollmentEventController(EnrollmentEventListener enrollmentEventListener) {
        this.enrollmentEventListener = enrollmentEventListener;
    }

    @PostMapping("/estado")
    public ResponseEntity<Void> receiveStatusChanged(@RequestBody @Valid EnrollmentStatusChangedEvent event) {
        enrollmentEventListener.onStatusChanged(event);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
