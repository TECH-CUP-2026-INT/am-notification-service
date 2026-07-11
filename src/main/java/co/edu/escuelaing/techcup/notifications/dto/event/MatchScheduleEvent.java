package co.edu.escuelaing.techcup.notifications.dto.event;

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
public record MatchScheduleEvent(
        @NotNull UUID matchId,
        @NotNull UUID teamHomeId,
        @NotNull UUID teamAwayId,
        @NotNull UUID recipientId,
        @NotNull MatchScheduleAction action,
        Instant scheduledAt,
        Instant previousScheduledAt,
        @NotNull Instant occurredAt
) {
}
