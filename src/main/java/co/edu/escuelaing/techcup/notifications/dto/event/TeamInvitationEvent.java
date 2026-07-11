package co.edu.escuelaing.techcup.notifications.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Equipos.
 * Endpoint: POST /api/notificaciones/equipos/invitaciones.
 */
public record TeamInvitationEvent(
        @NotNull UUID teamId,
        @NotBlank String teamName,
        @NotNull UUID invitedUserId,
        @NotNull UUID invitationId,
        @NotBlank String invitedBy,
        @NotNull Instant occurredAt
) {
}
