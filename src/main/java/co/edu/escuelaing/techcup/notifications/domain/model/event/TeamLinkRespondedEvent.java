package co.edu.escuelaing.techcup.notifications.domain.model.event;

import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Equipos. El
 * destinatario aquí es quien hizo la solicitud original (no el equipo).
 */
public record TeamLinkRespondedEvent(
        UUID teamId,
        String teamName,
        UUID requestId,
        UUID recipientId,
        boolean accepted,
        Instant respondedAt
) {
}
