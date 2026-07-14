package co.edu.escuelaing.techcup.notifications.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Equipos.
 * Endpoint: POST /api/notificaciones/equipos/solicitudes.
 *
 * <p>Abierto: ¿{@code recipientId} es el capitán del equipo, o hay que notificar a todo
 * el cuerpo técnico (varios POST, uno por destinatario)?
 */
public record TeamLinkRequestEvent(
        @NotNull UUID teamId,
        @NotBlank String teamName,
        @NotNull UUID requesterId,
        @NotBlank String requesterName,
        @NotNull UUID recipientId,
        @NotNull UUID requestId,
        @NotNull Instant occurredAt
) {
}
