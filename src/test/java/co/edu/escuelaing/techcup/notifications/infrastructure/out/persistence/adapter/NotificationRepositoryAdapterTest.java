package co.edu.escuelaing.techcup.notifications.infrastructure.out.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.escuelaing.techcup.notifications.domain.model.Notification;
import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.persistence.mongo.NotificationDocument;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.persistence.mongo.NotificationMongoRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationRepositoryAdapterTest {

    @Mock
    private NotificationMongoRepository notificationMongoRepository;

    private NotificationRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new NotificationRepositoryAdapter(notificationMongoRepository);
    }

    @Test
    void save_mapsDomainToDocumentAndBack() {
        Notification notification = new Notification();
        notification.setRecipientId(UUID.randomUUID());
        notification.setType(NotificationType.NUEVO_MENSAJE_CHAT);
        notification.setMessage("hola");
        notification.setCreatedAt(Instant.now());
        when(notificationMongoRepository.save(any(NotificationDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = adapter.save(notification);

        assertThat(result.getId()).isEqualTo(notification.getId());
        assertThat(result.getRecipientId()).isEqualTo(notification.getRecipientId());
        assertThat(result.getMessage()).isEqualTo("hola");
    }

    @Test
    void markAllAsRead_delegatesToMongoRepository() {
        UUID recipientId = UUID.randomUUID();
        Instant readAt = Instant.now();

        adapter.markAllAsRead(recipientId, readAt);

        verify(notificationMongoRepository).markAllAsRead(recipientId, readAt);
    }
}
