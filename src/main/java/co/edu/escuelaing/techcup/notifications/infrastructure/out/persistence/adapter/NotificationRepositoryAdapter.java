package co.edu.escuelaing.techcup.notifications.infrastructure.out.persistence.adapter;

import co.edu.escuelaing.techcup.notifications.domain.model.Notification;
import co.edu.escuelaing.techcup.notifications.domain.ports.out.NotificationRepositoryPort;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.persistence.mapper.NotificationPersistenceMapper;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.persistence.mongo.NotificationMongoRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final NotificationMongoRepository notificationMongoRepository;

    @Override
    public Notification save(Notification notification) {
        var saved = notificationMongoRepository.save(NotificationPersistenceMapper.toDocument(notification));
        return NotificationPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return notificationMongoRepository.findById(id).map(NotificationPersistenceMapper::toDomain);
    }

    @Override
    public List<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId) {
        return notificationMongoRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId).stream()
                .map(NotificationPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Notification> findByRecipientIdAndReadOrderByCreatedAtDesc(UUID recipientId, boolean read) {
        return notificationMongoRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(recipientId, read).stream()
                .map(NotificationPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public long countByRecipientIdAndReadFalse(UUID recipientId) {
        return notificationMongoRepository.countByRecipientIdAndReadFalse(recipientId);
    }

    @Override
    public void markAllAsRead(UUID recipientId, Instant readAt) {
        notificationMongoRepository.markAllAsRead(recipientId, readAt);
    }
}
