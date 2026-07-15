package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request;

import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamLinkRequestedEvent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Equipos.
 * Endpoint: POST /api/notificaciones/equipos/solicitudes.
 *
 * <p>Abierto: ¿{@code recipientId} es el capitán del equipo, o hay que notificar a todo
 * el cuerpo técnico (varios POST, uno por destinatario)?
 */
public record TeamLinkRequestedRequest(
        @NotNull UUID teamId,
        @NotBlank @Size(max = 120) String teamName,
        @NotNull UUID requesterId,
        @NotBlank @Size(max = 120) String requesterName,
        @NotNull UUID recipientId,
        @NotNull UUID requestId,
        @NotNull Instant occurredAt
) {
    public TeamLinkRequestedEvent toDomain() {
        return new TeamLinkRequestedEvent(teamId, teamName, requesterId, requesterName, recipientId, requestId, occurredAt);
    }
}
