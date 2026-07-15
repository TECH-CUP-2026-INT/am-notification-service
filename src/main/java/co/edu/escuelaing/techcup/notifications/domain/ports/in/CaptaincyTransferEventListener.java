package co.edu.escuelaing.techcup.notifications.domain.ports.in;

import co.edu.escuelaing.techcup.notifications.domain.model.event.CaptaincyTransferEvent;

/** Puerto de entrada para los eventos de cesión de capitanía del Servicio de Equipos. */
public interface CaptaincyTransferEventListener {

    void onCaptaincyTransferred(CaptaincyTransferEvent event);
}
