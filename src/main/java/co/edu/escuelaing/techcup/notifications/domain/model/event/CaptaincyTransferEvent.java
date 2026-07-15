package co.edu.escuelaing.techcup.notifications.domain.model.event;

import java.time.Instant;
import java.util.UUID;

/**
 * CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Equipos.
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
 */
public record CaptaincyTransferEvent(
        UUID teamId,
        String teamName,
        UUID currentCaptainId,
        UUID newCaptainId,
        CaptaincyTransferInitiator initiatedBy,
        Instant occurredAt
) {
}
