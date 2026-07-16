package co.edu.escuelaing.techcup.notifications.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;

/**
 * CONTRATO CONFIRMADO con el Servicio de Torneos: espejo del payload publicado por
 * SanctionNotificationAdapter allá, para sanciones {@code SanctionType.CONDUCT} — el
 * Organizador decide, después del hecho, cuántos partidos se suspende a un jugador. No
 * está ligada a un partido específico en vivo, a diferencia de PlayerSanctionedEvent
 * (tarjetas, detectadas por el Servicio de Partidos), que sigue siendo el único origen
 * para sanciones automáticas (RED_CARD / YELLOW_CARD_ACCUMULATION); este servicio no debe
 * usar este evento para esos dos tipos, para evitar notificar dos veces al jugador.
 *
 * <p>{@code playerId} es {@code String} (no UUID) porque así lo modela el dominio de
 * Torneos. Igual que con PlayerSanctionedEvent, este servicio asume que corresponde al
 * mismo UUID de cuenta de la plataforma y lo usa directamente como destinatario (ver
 * ConductSanctionEventListenerImpl); si Torneos llega a usar un id de dominio propio
 * no-UUID, esa conversión fallará y habrá que resolver el playerId contra otro servicio
 * antes de notificar.
 */
public record PlayerConductSanctionedEvent(
        @NotBlank String playerId,
        @Positive int matchesSuspended,
        @NotBlank String reason,
        @NotNull Instant occurredAt
) {
}
