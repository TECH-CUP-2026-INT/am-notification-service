package co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.dto;

import co.edu.escuelaing.techcup.notifications.domain.model.event.ChatMessageEvent;
import java.time.Instant;
import java.util.UUID;

/** Payload versionado de la cola notificaciones.mensajes.q. */
public record ChatMessageMessageV1(
        int schemaVersion,
        UUID chatId,
        UUID senderId,
        String senderName,
        UUID recipientId,
        String messagePreview,
        Instant sentAt
) {
    public ChatMessageEvent toDomain() {
        return new ChatMessageEvent(chatId, senderId, senderName, recipientId, messagePreview, sentAt);
    }
}
