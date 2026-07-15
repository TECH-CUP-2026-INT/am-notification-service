package co.edu.escuelaing.techcup.notifications.domain.service;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentProofReceivedEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentStatusChangedEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CreateNotificationCommand;
import org.springframework.stereotype.Service;

/** Traduce los eventos de inscripción al comando de notificación correspondiente. */
@Service
public class EnrollmentNotificationComposer {

    public CreateNotificationCommand composeStatusChanged(EnrollmentStatusChangedEvent event) {
        NotificationType type = switch (event.newStatus()) {
            case APROBADA -> NotificationType.INSCRIPCION_APROBADA;
            case RECHAZADA -> NotificationType.INSCRIPCION_RECHAZADA;
            case CANCELADA -> NotificationType.INSCRIPCION_CANCELADA;
        };

        String message = switch (event.newStatus()) {
            case APROBADA -> "Tu inscripción fue aprobada.";
            case RECHAZADA -> "Tu inscripción fue rechazada"
                    + (event.reason() != null ? ": " + event.reason() : ".");
            case CANCELADA -> "Tu inscripción fue cancelada"
                    + (event.reason() != null ? ": " + event.reason() : ".");
        };

        return new CreateNotificationCommand(event.recipientId(), type, message, event.enrollmentId());
    }

    public CreateNotificationCommand composeProofReceived(EnrollmentProofReceivedEvent event) {
        String message = "Recibimos el comprobante de tu inscripción. "
                + "Un organizador la revisará pronto (estado: pendiente).";

        return new CreateNotificationCommand(
                event.recipientId(), NotificationType.INSCRIPCION_COMPROBANTE_RECIBIDO, message, event.enrollmentId());
    }
}
