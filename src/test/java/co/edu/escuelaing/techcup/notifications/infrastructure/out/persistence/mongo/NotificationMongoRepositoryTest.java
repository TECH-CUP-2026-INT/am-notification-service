package co.edu.escuelaing.techcup.notifications.infrastructure.out.persistence.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Ejercita la query custom @Query/@Update markAllAsRead contra un Mongo real (no un mock). */
@Testcontainers
@DataMongoTest
@EntityScan(basePackageClasses = NotificationDocument.class)
@Import(NotificationMongoRepository.class)
class NotificationMongoRepositoryTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:7");

    @Autowired
    private NotificationMongoRepository repository;

    @Test
    void markAllAsRead_onlyUpdatesUnreadNotificationsOfThatRecipient() {
        UUID recipientId = UUID.randomUUID();
        UUID otherRecipientId = UUID.randomUUID();

        NotificationDocument unread = newDocument(recipientId, false);
        NotificationDocument alreadyRead = newDocument(recipientId, true);
        NotificationDocument otherUserUnread = newDocument(otherRecipientId, false);
        repository.saveAll(java.util.List.of(unread, alreadyRead, otherUserUnread));

        Instant readAt = Instant.now();
        repository.markAllAsRead(recipientId, readAt);

        NotificationDocument reloadedUnread = repository.findById(unread.getId()).orElseThrow();
        NotificationDocument reloadedOtherUser = repository.findById(otherUserUnread.getId()).orElseThrow();
        assertThat(reloadedUnread.isRead()).isTrue();
        assertThat(reloadedUnread.getReadAt()).isEqualTo(readAt);
        assertThat(reloadedOtherUser.isRead()).isFalse();
    }

    private static NotificationDocument newDocument(UUID recipientId, boolean read) {
        NotificationDocument document = new NotificationDocument();
        document.setRecipientId(recipientId);
        document.setType(NotificationType.NUEVO_MENSAJE_CHAT);
        document.setMessage("hola");
        document.setRead(read);
        document.setCreatedAt(Instant.now());
        return document;
    }
}
