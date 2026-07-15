package co.edu.escuelaing.techcup.notifications.domain.ports.in;

import co.edu.escuelaing.techcup.notifications.domain.model.Notification;
import java.util.List;
import java.util.UUID;

/** Puerto de entrada con los casos de uso de negocio sobre notificaciones. */
public interface NotificationUseCase {

    Notification create(CreateNotificationCommand command);

    List<Notification> listForUser(UUID recipientId, Boolean read);

    long countUnread(UUID recipientId);

    Notification markAsRead(UUID notificationId, UUID recipientId);

    void markAllAsRead(UUID recipientId);
}
