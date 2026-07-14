package co.edu.escuelaing.techcup.notifications.listener;

import co.edu.escuelaing.techcup.notifications.dto.event.EnrollmentProofReceivedEvent;
import co.edu.escuelaing.techcup.notifications.dto.event.EnrollmentStatusChangedEvent;

/** Puerto de entrada para los eventos de inscripción del Servicio de Inscripción. */
public interface EnrollmentEventListener {

    void onStatusChanged(EnrollmentStatusChangedEvent event);

    void onProofReceived(EnrollmentProofReceivedEvent event);
}
