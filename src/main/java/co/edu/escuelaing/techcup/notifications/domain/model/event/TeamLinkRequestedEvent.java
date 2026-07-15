package co.edu.escuelaing.techcup.notifications.domain.model.event;

import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Equipos.
 *
 * <p>Abierto: ¿{@code recipientId} es el capitán del equipo, o hay que notificar a todo
 * el cuerpo técnico (varios envíos, uno por destinatario)?
 */
public record TeamLinkRequestedEvent(
        UUID teamId,
        String teamName,
        UUID requesterId,
        String requesterName,
        UUID recipientId,
        UUID requestId,
        Instant occurredAt
) {
}
