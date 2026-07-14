package co.edu.escuelaing.techcup.notifications.mapper;

import co.edu.escuelaing.techcup.notifications.dto.response.NotificationResponse;
import co.edu.escuelaing.techcup.notifications.entity.Notification;

public final class NotificationMapper {

    private NotificationMapper() {
    }

    public static NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getReferenceId(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt());
    }
}
