package co.edu.escuelaing.techcup.notifications.exception;

import java.util.UUID;

public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(UUID notificationId) {
        super("No existe la notificación " + notificationId);
    }
}
