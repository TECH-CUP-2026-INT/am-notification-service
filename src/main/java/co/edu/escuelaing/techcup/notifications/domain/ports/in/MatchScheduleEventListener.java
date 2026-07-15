package co.edu.escuelaing.techcup.notifications.domain.ports.in;

import co.edu.escuelaing.techcup.notifications.domain.model.event.MatchScheduleEvent;

/** Puerto de entrada para los eventos de agendamiento del Servicio de Agendamiento. */
public interface MatchScheduleEventListener {

    void onScheduleChanged(MatchScheduleEvent event);
}
