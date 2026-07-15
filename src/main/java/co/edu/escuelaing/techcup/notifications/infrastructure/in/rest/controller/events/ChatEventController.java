package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.controller.events;

import co.edu.escuelaing.techcup.notifications.domain.ports.in.ChatEventListener;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.ChatMessageRequest;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.swagger.ChatEventApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Webhook propuesto para el Servicio de Comunicaciones. Contrato pendiente de
 * confirmar: ver ChatMessageRequest. Segunda puerta de entrada equivalente:
 * infrastructure/out/messaging/consumer/ChatEventConsumer (RabbitMQ).
 */
@RestController
@RequestMapping("/api/notificaciones/mensajes")
@RequiredArgsConstructor
public class ChatEventController implements ChatEventApi {

    private final ChatEventListener chatEventListener;

    @Override
    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody @Valid ChatMessageRequest request) {
        chatEventListener.onNewChatMessage(request.toDomain());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
