package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request;

import co.edu.escuelaing.techcup.notifications.domain.model.event.MatchScheduleAction;
import co.edu.escuelaing.techcup.notifications.domain.model.event.MatchScheduleEvent;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de
 * Agendamiento. Endpoint: POST /api/notificaciones/partidos.
 *
 * <p>Abierto: ¿el destinatario es cada jugador/capitán de ambos equipos (un POST por
 * destinatario, igual que se asume para Equipos), o solo los capitanes? {@code
 * scheduledAt} es obligatorio salvo cuando {@code action} es CANCELADO;
 * {@code previousScheduledAt} solo aplica cuando {@code action} es REPROGRAMADO.
 */
public record MatchScheduleRequest(
        @NotNull UUID matchId,
        @NotNull UUID teamHomeId,
        @NotNull UUID teamAwayId,
        @NotNull UUID recipientId,
        @NotNull MatchScheduleAction action,
        Instant scheduledAt,
        Instant previousScheduledAt,
        @NotNull Instant occurredAt
) {
    @AssertTrue(message = "scheduledAt es obligatorio salvo cuando action es CANCELADO")
    public boolean isScheduledAtValid() {
        return action == null || action == MatchScheduleAction.CANCELADO || scheduledAt != null;
    }

    @AssertTrue(message = "previousScheduledAt solo aplica cuando action es REPROGRAMADO")
    public boolean isPreviousScheduledAtValid() {
        return previousScheduledAt == null || action == MatchScheduleAction.REPROGRAMADO;
    }

    public MatchScheduleEvent toDomain() {
        return new MatchScheduleEvent(
                matchId, teamHomeId, teamAwayId, recipientId, action, scheduledAt, previousScheduledAt, occurredAt);
    }
}
