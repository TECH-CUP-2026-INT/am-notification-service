package co.edu.escuelaing.techcup.notifications.application.usecase;

import co.edu.escuelaing.techcup.notifications.domain.exception.NotificationAccessDeniedException;
import co.edu.escuelaing.techcup.notifications.domain.exception.NotificationNotFoundException;
import co.edu.escuelaing.techcup.notifications.domain.model.Notification;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CreateNotificationCommand;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.NotificationUseCase;
import co.edu.escuelaing.techcup.notifications.domain.ports.out.NotificationEventPublisherPort;
import co.edu.escuelaing.techcup.notifications.domain.ports.out.NotificationRepositoryPort;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationUseCase {

    private final NotificationRepositoryPort notificationRepositoryPort;
    private final NotificationEventPublisherPort notificationEventPublisherPort;

    @Override
    public Notification create(CreateNotificationCommand command) {
        if (command.recipientId() == null || command.message() == null || command.message().isBlank()) {
            throw new IllegalStateException(
                    "El adaptador de entrada construyó un CreateNotificationCommand incompleto: "
                            + "recipientId y message son obligatorios.");
        }

        Notification notification = new Notification();
        notification.setRecipientId(command.recipientId());
        notification.setType(command.type());
        notification.setMessage(command.message());
        notification.setReferenceId(command.referenceId());
        notification.setCreatedAt(Instant.now());

        Notification saved = notificationRepositoryPort.save(notification);
        notificationEventPublisherPort.publishNotificationCreated(saved);
        return saved;
    }

    @Override
    public List<Notification> listForUser(UUID recipientId, Boolean read) {
        if (read == null) {
            return notificationRepositoryPort.findByRecipientIdOrderByCreatedAtDesc(recipientId);
        }
        return notificationRepositoryPort.findByRecipientIdAndReadOrderByCreatedAtDesc(recipientId, read);
    }

    @Override
    public long countUnread(UUID recipientId) {
        return notificationRepositoryPort.countByRecipientIdAndReadFalse(recipientId);
    }

    @Override
    public Notification markAsRead(UUID notificationId, UUID recipientId) {
        Notification notification = notificationRepositoryPort.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (!notification.getRecipientId().equals(recipientId)) {
            throw new NotificationAccessDeniedException(notificationId);
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(Instant.now());
            notification = notificationRepositoryPort.save(notification);
        }
        return notification;
    }

    @Override
    public void markAllAsRead(UUID recipientId) {
        notificationRepositoryPort.markAllAsRead(recipientId, Instant.now());
    }
}
