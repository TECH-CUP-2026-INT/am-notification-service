package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request;

import co.edu.escuelaing.techcup.notifications.domain.model.event.CaptaincyTransferEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.CaptaincyTransferInitiator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Equipos.
 * Endpoint: POST /api/notificaciones/equipos/capitania.
 *
 * <p>Cubre RF-09 de la hoja de requerimientos: "Si el Capitán delega: notifica al jugador
 * elegido. Si el jugador aplica: notifica al Capitán actual." Un mismo evento de origen
 * (cambio de capitanía) dispara dos direcciones de notificación distintas según quién
 * inició el cambio, indicado por {@link #initiatedBy()}.
 */
public record CaptaincyTransferRequest(
        @NotNull UUID teamId,
        @NotBlank @Size(max = 120) String teamName,
        @NotNull UUID currentCaptainId,
        @NotNull UUID newCaptainId,
        @NotNull CaptaincyTransferInitiator initiatedBy,
        @NotNull Instant occurredAt
) {
    public CaptaincyTransferEvent toDomain() {
        return new CaptaincyTransferEvent(teamId, teamName, currentCaptainId, newCaptainId, initiatedBy, occurredAt);
    }
}
