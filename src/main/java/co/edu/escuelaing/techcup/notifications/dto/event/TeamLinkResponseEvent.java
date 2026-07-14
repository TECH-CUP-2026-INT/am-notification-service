package co.edu.escuelaing.techcup.notifications.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Equipos.
 * Endpoint: POST /api/notificaciones/equipos/respuestas. El destinatario aquí es quien
 * hizo la solicitud original (no el equipo).
 */
public record TeamLinkResponseEvent(
        @NotNull UUID teamId,
        @NotBlank String teamName,
        @NotNull UUID requestId,
        @NotNull UUID recipientId,
        boolean accepted,
        @NotNull Instant respondedAt
) {
}
