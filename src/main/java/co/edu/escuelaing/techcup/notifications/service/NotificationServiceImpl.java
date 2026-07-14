package co.edu.escuelaing.techcup.notifications.service;

import co.edu.escuelaing.techcup.notifications.entity.Notification;
import co.edu.escuelaing.techcup.notifications.exception.NotificationAccessDeniedException;
import co.edu.escuelaing.techcup.notifications.exception.NotificationNotFoundException;
import co.edu.escuelaing.techcup.notifications.repository.NotificationRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public Notification create(CreateNotificationCommand command) {
        Notification notification = new Notification();
        notification.setRecipientId(command.recipientId());
        notification.setType(command.type());
        notification.setMessage(command.message());
        notification.setReferenceId(command.referenceId());
        notification.setCreatedAt(Instant.now());
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> listForUser(UUID recipientId, Boolean read) {
        if (read == null) {
            return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
        }
        return notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(recipientId, read);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(UUID recipientId) {
        return notificationRepository.countByRecipientIdAndReadFalse(recipientId);
    }

    @Override
    @Transactional
    public Notification markAsRead(UUID notificationId, UUID recipientId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (!notification.getRecipientId().equals(recipientId)) {
            throw new NotificationAccessDeniedException(notificationId);
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(Instant.now());
        }
        return notification;
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID recipientId) {
        notificationRepository.markAllAsRead(recipientId, Instant.now());
    }
}
