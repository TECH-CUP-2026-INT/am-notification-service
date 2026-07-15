package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request;

import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamInvitationEvent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Equipos.
 * Endpoint: POST /api/notificaciones/equipos/invitaciones.
 */
public record TeamInvitationRequest(
        @NotNull UUID teamId,
        @NotBlank @Size(max = 120) String teamName,
        @NotNull UUID invitedUserId,
        @NotNull UUID invitationId,
        @NotBlank @Size(max = 120) String invitedBy,
        @NotNull Instant occurredAt
) {
    public TeamInvitationEvent toDomain() {
        return new TeamInvitationEvent(teamId, teamName, invitedUserId, invitationId, invitedBy, occurredAt);
    }
}
