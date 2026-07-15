package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request;

import co.edu.escuelaing.techcup.notifications.domain.model.event.ChatMessageEvent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de
 * Comunicaciones. Endpoint: POST /api/notificaciones/mensajes.
 *
 * <p>Abierto: ¿{@code recipientId} es un único usuario, o para chats grupales el
 * Servicio de Comunicaciones nos hace un POST por cada miembro del chat (fan-out en
 * origen)? ¿nos envían el texto completo del mensaje o solo un preview truncado?
 */
public record ChatMessageRequest(
        @NotNull UUID chatId,
        @NotNull UUID senderId,
        @NotBlank @Size(max = 120) String senderName,
        @NotNull UUID recipientId,
        @NotBlank @Size(max = 500) String messagePreview,
        @NotNull Instant sentAt
) {
    public ChatMessageEvent toDomain() {
        return new ChatMessageEvent(chatId, senderId, senderName, recipientId, messagePreview, sentAt);
    }
}
