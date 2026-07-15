package co.edu.escuelaing.techcup.notifications.domain.ports.in;

import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentProofReceivedEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentStatusChangedEvent;

/** Puerto de entrada para los eventos de inscripción del Servicio de Inscripción. */
public interface EnrollmentEventListener {

    void onStatusChanged(EnrollmentStatusChangedEvent event);

    void onProofReceived(EnrollmentProofReceivedEvent event);
}
