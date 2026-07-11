package co.edu.escuelaing.techcup.notifications.listener;

import co.edu.escuelaing.techcup.notifications.dto.event.MatchScheduleEvent;

/** Puerto de entrada para los eventos de agendamiento del Servicio de Agendamiento. */
public interface MatchScheduleEventListener {

    void onScheduleChanged(MatchScheduleEvent event);
}
