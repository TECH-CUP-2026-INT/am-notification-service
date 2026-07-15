package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.controller.events;

import co.edu.escuelaing.techcup.notifications.domain.ports.in.SanctionEventListener;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.PlayerSanctionedRequest;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.swagger.SanctionEventApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Webhook consumido por el Servicio de Partidos (RestSanctionNotifier). Contrato
 * confirmado: ver PlayerSanctionedRequest. Segunda puerta de entrada equivalente:
 * infrastructure/out/messaging/consumer/SanctionEventConsumer (RabbitMQ).
 */
@RestController
@RequestMapping("/api/notificaciones/sanciones")
@RequiredArgsConstructor
public class SanctionEventController implements SanctionEventApi {

    private final SanctionEventListener sanctionEventListener;

    @Override
    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody @Valid PlayerSanctionedRequest request) {
        sanctionEventListener.onPlayerSanctioned(request.toDomain());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
