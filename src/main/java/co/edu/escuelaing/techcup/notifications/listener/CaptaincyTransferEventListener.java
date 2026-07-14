package co.edu.escuelaing.techcup.notifications.listener;

import co.edu.escuelaing.techcup.notifications.dto.event.CaptaincyTransferEvent;

/** Puerto de entrada para los eventos de cesión de capitanía del Servicio de Equipos. */
public interface CaptaincyTransferEventListener {

    void onCaptaincyTransferred(CaptaincyTransferEvent event);
}
