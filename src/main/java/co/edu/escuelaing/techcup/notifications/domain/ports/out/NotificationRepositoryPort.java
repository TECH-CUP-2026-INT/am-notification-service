package co.edu.escuelaing.techcup.notifications.domain.ports.out;

import co.edu.escuelaing.techcup.notifications.domain.model.Notification;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de salida hacia la persistencia de notificaciones, sin tipos de Spring Data. */
public interface NotificationRepositoryPort {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID id);

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    List<Notification> findByRecipientIdAndReadOrderByCreatedAtDesc(UUID recipientId, boolean read);

    long countByRecipientIdAndReadFalse(UUID recipientId);

    void markAllAsRead(UUID recipientId, Instant readAt);
}
