package co.edu.escuelaing.techcup.notifications.repository;

import co.edu.escuelaing.techcup.notifications.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

public interface NotificationRepository extends MongoRepository<Notification, UUID> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    List<Notification> findByRecipientIdAndReadOrderByCreatedAtDesc(UUID recipientId, boolean read);

    long countByRecipientIdAndReadFalse(UUID recipientId);

    @Query("{ 'recipientId': ?0, 'read': false }")
    @Update("{ '$set': { 'read': true, 'readAt': ?1 } }")
    void markAllAsRead(UUID recipientId, Instant readAt);
}
