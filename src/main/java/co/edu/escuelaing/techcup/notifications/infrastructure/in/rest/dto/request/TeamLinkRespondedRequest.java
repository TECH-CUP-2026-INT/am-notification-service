package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request;

import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamLinkRespondedEvent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Equipos.
 * Endpoint: POST /api/notificaciones/equipos/respuestas. El destinatario aquí es quien
 * hizo la solicitud original (no el equipo).
 */
public record TeamLinkRespondedRequest(
        @NotNull UUID teamId,
        @NotBlank @Size(max = 120) String teamName,
        @NotNull UUID requestId,
        @NotNull UUID recipientId,
        boolean accepted,
        @NotNull Instant respondedAt
) {
    public TeamLinkRespondedEvent toDomain() {
        return new TeamLinkRespondedEvent(teamId, teamName, requestId, recipientId, accepted, respondedAt);
    }
}
