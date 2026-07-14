package co.edu.escuelaing.techcup.notifications.listener;

import co.edu.escuelaing.techcup.notifications.dto.event.PlayerSanctionedEvent;

/** Puerto de entrada para el evento de sanción por tarjetas del Servicio de Partidos. */
public interface SanctionEventListener {

    void onPlayerSanctioned(PlayerSanctionedEvent event);
}
