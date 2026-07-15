package co.edu.escuelaing.techcup.notifications.domain.model.event;

import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Equipos.
 */
public record TeamInvitationEvent(
        UUID teamId,
        String teamName,
        UUID invitedUserId,
        UUID invitationId,
        String invitedBy,
        Instant occurredAt
) {
}
