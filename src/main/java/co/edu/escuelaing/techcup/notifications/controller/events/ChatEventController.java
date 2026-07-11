package co.edu.escuelaing.techcup.notifications.controller.events;

import co.edu.escuelaing.techcup.notifications.dto.event.ChatMessageEvent;
import co.edu.escuelaing.techcup.notifications.listener.ChatEventListener;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Webhook propuesto para el Servicio de Comunicaciones. Contrato pendiente de
 * confirmar: ver ChatMessageEvent.
 */
@RestController
@RequestMapping("/api/notificaciones/mensajes")
public class ChatEventController {

    private final ChatEventListener chatEventListener;

    public ChatEventController(ChatEventListener chatEventListener) {
        this.chatEventListener = chatEventListener;
    }

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody @Valid ChatMessageEvent event) {
        chatEventListener.onNewChatMessage(event);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
