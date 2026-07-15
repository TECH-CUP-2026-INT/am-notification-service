package co.edu.escuelaing.techcup.notifications.domain.ports.in;

import co.edu.escuelaing.techcup.notifications.domain.model.event.PlayerSanctionedEvent;

/** Puerto de entrada para el evento de sanción por tarjetas del Servicio de Partidos. */
public interface SanctionEventListener {

    void onPlayerSanctioned(PlayerSanctionedEvent event);
}
