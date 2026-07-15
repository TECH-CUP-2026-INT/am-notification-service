package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.controller.events;

import co.edu.escuelaing.techcup.notifications.domain.ports.in.MatchScheduleEventListener;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.MatchScheduleRequest;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.swagger.MatchScheduleEventApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Webhook propuesto para el Servicio de Agendamiento. Contrato pendiente de confirmar:
 * ver MatchScheduleRequest. Segunda puerta de entrada equivalente:
 * infrastructure/out/messaging/consumer/MatchScheduleEventConsumer (RabbitMQ).
 */
@RestController
@RequestMapping("/api/notificaciones/partidos")
@RequiredArgsConstructor
public class MatchScheduleEventController implements MatchScheduleEventApi {

    private final MatchScheduleEventListener matchScheduleEventListener;

    @Override
    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody @Valid MatchScheduleRequest request) {
        matchScheduleEventListener.onScheduleChanged(request.toDomain());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
