package co.edu.escuelaing.techcup.notifications.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public record ChatMessageEvent(
        @NotNull UUID chatId,
        @NotNull UUID senderId,
        @NotBlank String senderName,
        @NotNull UUID recipientId,
        @NotBlank String messagePreview,
        @NotNull Instant sentAt
) {
}
