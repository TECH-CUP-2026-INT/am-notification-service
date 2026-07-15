package co.edu.escuelaing.techcup.notifications.domain.model.event;

import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de
 * Agendamiento.
 *
 * <p>Abierto: ¿el destinatario es cada jugador/capitán de ambos equipos (un envío por
 * destinatario, igual que se asume para Equipos), o solo los capitanes? {@code
 * scheduledAt} es obligatorio salvo cuando {@code action} es CANCELADO;
 * {@code previousScheduledAt} solo aplica cuando {@code action} es REPROGRAMADO.
 */
public record MatchScheduleEvent(
        UUID matchId,
        UUID teamHomeId,
        UUID teamAwayId,
        UUID recipientId,
        MatchScheduleAction action,
        Instant scheduledAt,
        Instant previousScheduledAt,
        Instant occurredAt
) {
}
