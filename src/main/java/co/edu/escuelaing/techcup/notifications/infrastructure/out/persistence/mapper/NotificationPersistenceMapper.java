package co.edu.escuelaing.techcup.notifications.infrastructure.out.persistence.mapper;

import co.edu.escuelaing.techcup.notifications.domain.model.Notification;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.persistence.mongo.NotificationDocument;

public final class NotificationPersistenceMapper {

    private NotificationPersistenceMapper() {
    }

    public static NotificationDocument toDocument(Notification notification) {
        NotificationDocument document = new NotificationDocument();
        document.setId(notification.getId());
        document.setRecipientId(notification.getRecipientId());
        document.setType(notification.getType());
        document.setMessage(notification.getMessage());
        document.setReferenceId(notification.getReferenceId());
        document.setRead(notification.isRead());
        document.setCreatedAt(notification.getCreatedAt());
        document.setReadAt(notification.getReadAt());
        return document;
    }

    public static Notification toDomain(NotificationDocument document) {
        Notification notification = new Notification();
        notification.setId(document.getId());
        notification.setRecipientId(document.getRecipientId());
        notification.setType(document.getType());
        notification.setMessage(document.getMessage());
        notification.setReferenceId(document.getReferenceId());
        notification.setRead(document.isRead());
        notification.setCreatedAt(document.getCreatedAt());
        notification.setReadAt(document.getReadAt());
        return notification;
    }
}
