package co.edu.escuelaing.techcup.notifications.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Equipos.
 * Endpoint: POST /api/notificaciones/equipos/capitania.
 *
 * <p>Cubre RF-09 de la hoja de requerimientos: "Si el Capitán delega: notifica al jugador
 * elegido. Si el jugador aplica: notifica al Capitán actual." Un mismo evento de origen
 * (cambio de capitanía) dispara dos direcciones de notificación distintas según quién
 * inició el cambio, indicado por {@link #initiatedBy()}:
 *
 * <ul>
 *   <li>{@link CaptaincyTransferInitiator#DELEGATION}: el Capitán actual
 *       ({@code currentCaptainId}) delega el rol en otro jugador — se notifica a
 *       {@code newCaptainId}.</li>
 *   <li>{@link CaptaincyTransferInitiator#APPLICATION}: un jugador aplica para ser Capitán
 *       (propuesto como {@code newCaptainId}) — se notifica al Capitán actual
 *       ({@code currentCaptainId}).</li>
 * </ul>
 *
 * <p>Abierto: el requerimiento de la hoja no menciona un paso de aceptación/rechazo (a
 * diferencia de {@link TeamLinkRequestEvent}/{@link TeamLinkResponseEvent}, que sí
 * modelan solicitud + respuesta), por lo que se asume que este es un único evento
 * informativo — el cambio de capitanía ya ocurrió (o fue propuesto) del lado del Servicio
 * de Equipos, y esto es solo la notificación, no una solicitud que este servicio deba
 * aprobar. Preguntas abiertas para el equipo dueño:
 * <ul>
 *   <li>¿La delegación es efectiva de inmediato, o el jugador elegido debe aceptarla en
 *       un paso posterior (lo que requeriría un segundo evento de respuesta, como en el
 *       flujo de vinculación)?</li>
 *   <li>¿La aplicación de un jugador requiere que el Capitán actual apruebe el cambio, o
 *       es solo informativa (p. ej. una votación automática del equipo)?</li>
 * </ul>
 * Mientras no se confirme, este servicio trata ambos casos como notificación directa de
 * un hecho, sin flujo de solicitud/respuesta propio.
 */
public record CaptaincyTransferEvent(
        @NotNull UUID teamId,
        @NotBlank String teamName,
        @NotNull UUID currentCaptainId,
        @NotNull UUID newCaptainId,
        @NotNull CaptaincyTransferInitiator initiatedBy,
        @NotNull Instant occurredAt
) {
}
