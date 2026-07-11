package co.edu.escuelaing.techcup.notifications.listener;

import co.edu.escuelaing.techcup.notifications.dto.event.EnrollmentProofReceivedEvent;
import co.edu.escuelaing.techcup.notifications.dto.event.EnrollmentStatusChangedEvent;
import co.edu.escuelaing.techcup.notifications.entity.enums.NotificationType;
import co.edu.escuelaing.techcup.notifications.service.CreateNotificationCommand;
import co.edu.escuelaing.techcup.notifications.service.NotificationService;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentEventListenerImpl implements EnrollmentEventListener {

    private final NotificationService notificationService;

    public EnrollmentEventListenerImpl(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void onStatusChanged(EnrollmentStatusChangedEvent event) {
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

        notificationService.create(new CreateNotificationCommand(
                event.recipientId(), type, message, event.enrollmentId()));
    }

    @Override
    public void onProofReceived(EnrollmentProofReceivedEvent event) {
        String message = "Recibimos el comprobante de tu inscripción. "
                + "Un organizador la revisará pronto (estado: pendiente).";

        notificationService.create(new CreateNotificationCommand(
                event.recipientId(), NotificationType.INSCRIPCION_COMPROBANTE_RECIBIDO, message, event.enrollmentId()));
    }
}
