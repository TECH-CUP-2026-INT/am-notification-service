package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.controller.events;

import co.edu.escuelaing.techcup.notifications.domain.ports.in.EnrollmentEventListener;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.EnrollmentProofReceivedRequest;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.EnrollmentStatusChangedRequest;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.swagger.EnrollmentEventApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Webhooks propuestos para el Servicio de Inscripción. Contratos pendientes de
 * confirmar: ver EnrollmentStatusChangedRequest, EnrollmentProofReceivedRequest.
 * Segunda puerta de entrada equivalente: infrastructure/out/messaging/consumer
 * (RabbitMQ).
 */
@RestController
@RequestMapping("/api/notificaciones/inscripciones")
@RequiredArgsConstructor
public class EnrollmentEventController implements EnrollmentEventApi {

    private final EnrollmentEventListener enrollmentEventListener;

    @Override
    @PostMapping("/estado")
    public ResponseEntity<Void> receiveStatusChanged(@RequestBody @Valid EnrollmentStatusChangedRequest request) {
        enrollmentEventListener.onStatusChanged(request.toDomain());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Override
    @PostMapping("/comprobante")
    public ResponseEntity<Void> receiveProofReceived(@RequestBody @Valid EnrollmentProofReceivedRequest request) {
        enrollmentEventListener.onProofReceived(request.toDomain());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
