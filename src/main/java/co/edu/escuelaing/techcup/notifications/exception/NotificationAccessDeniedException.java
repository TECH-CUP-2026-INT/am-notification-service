package co.edu.escuelaing.techcup.notifications.exception;

import java.util.UUID;

public class NotificationAccessDeniedException extends RuntimeException {

    public NotificationAccessDeniedException(UUID notificationId) {
        super("No tiene permisos sobre la notificación " + notificationId);
    }
}
