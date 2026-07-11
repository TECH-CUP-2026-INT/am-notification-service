package co.edu.escuelaing.techcup.notifications.repository;

import co.edu.escuelaing.techcup.notifications.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    List<Notification> findByRecipientIdAndReadOrderByCreatedAtDesc(UUID recipientId, boolean read);

    long countByRecipientIdAndReadFalse(UUID recipientId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :readAt "
            + "WHERE n.recipientId = :recipientId AND n.read = false")
    void markAllAsRead(@Param("recipientId") UUID recipientId, @Param("readAt") Instant readAt);
}
