package co.edu.escuelaing.techcup.notifications.domain.exception;

import java.util.UUID;

public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(UUID notificationId) {
        super("No existe la notificación " + notificationId);
    }
}
