package co.edu.escuelaing.techcup.notifications.service;

import co.edu.escuelaing.techcup.notifications.entity.Notification;
import java.util.List;
import java.util.UUID;

public interface NotificationService {

    Notification create(CreateNotificationCommand command);

    List<Notification> listForUser(UUID recipientId, Boolean read);

    long countUnread(UUID recipientId);

    Notification markAsRead(UUID notificationId, UUID recipientId);

    void markAllAsRead(UUID recipientId);
}
