package co.edu.escuelaing.techcup.notifications.domain.model.event;

import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de
 * Comunicaciones.
 *
 * <p>Abierto: ¿{@code recipientId} es un único usuario, o para chats grupales el
 * Servicio de Comunicaciones nos hace un envío por cada miembro del chat (fan-out en
 * origen)? ¿nos envían el texto completo del mensaje o solo un preview truncado?
 */
public record ChatMessageEvent(
        UUID chatId,
        UUID senderId,
        String senderName,
        UUID recipientId,
        String messagePreview,
        Instant sentAt
) {
}
