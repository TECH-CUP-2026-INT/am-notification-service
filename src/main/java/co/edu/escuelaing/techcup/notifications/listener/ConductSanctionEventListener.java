package co.edu.escuelaing.techcup.notifications.listener;

import co.edu.escuelaing.techcup.notifications.dto.event.PlayerConductSanctionedEvent;

/** Puerto de entrada para el evento de sanción por conducta del Servicio de Torneos. */
public interface ConductSanctionEventListener {

    void onPlayerConductSanctioned(PlayerConductSanctionedEvent event);
}
