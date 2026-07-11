package co.edu.escuelaing.techcup.notifications.controller.events;

import co.edu.escuelaing.techcup.notifications.dto.event.EnrollmentProofReceivedEvent;
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
 * Webhooks propuestos para el Servicio de Inscripción. Contratos pendientes de
 * confirmar: ver EnrollmentStatusChangedEvent, EnrollmentProofReceivedEvent.
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

    @PostMapping("/comprobante")
    public ResponseEntity<Void> receiveProofReceived(@RequestBody @Valid EnrollmentProofReceivedEvent event) {
        enrollmentEventListener.onProofReceived(event);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
