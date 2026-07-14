package co.edu.escuelaing.techcup.notifications.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.escuelaing.techcup.notifications.entity.Notification;
import co.edu.escuelaing.techcup.notifications.entity.enums.NotificationType;
import co.edu.escuelaing.techcup.notifications.exception.NotificationAccessDeniedException;
import co.edu.escuelaing.techcup.notifications.exception.NotificationNotFoundException;
import co.edu.escuelaing.techcup.notifications.repository.NotificationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationServiceImpl notificationService;

    private final UUID recipientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(notificationRepository);
    }

    @Test
    void create_persistsNotificationWithCommandFields() {
        UUID referenceId = UUID.randomUUID();
        CreateNotificationCommand command = new CreateNotificationCommand(
                recipientId, NotificationType.NUEVO_MENSAJE_CHAT, "hola", referenceId);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.create(command);

        assertThat(result.getRecipientId()).isEqualTo(recipientId);
        assertThat(result.getType()).isEqualTo(NotificationType.NUEVO_MENSAJE_CHAT);
        assertThat(result.getMessage()).isEqualTo("hola");
        assertThat(result.getReferenceId()).isEqualTo(referenceId);
        assertThat(result.isRead()).isFalse();
    }

    @Test
    void listForUser_withoutFilter_returnsFullHistoryOrderedByRepository() {
        List<Notification> expected = List.of(new Notification());
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId)).thenReturn(expected);

        List<Notification> result = notificationService.listForUser(recipientId, null);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void listForUser_withFilter_delegatesToFilteredQuery() {
        List<Notification> expected = List.of(new Notification());
        when(notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(recipientId, false))
                .thenReturn(expected);

        List<Notification> result = notificationService.listForUser(recipientId, false);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void markAsRead_ownedAndUnread_marksReadAndStampsTimestamp() {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setRead(false);
        UUID notificationId = UUID.randomUUID();
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        Notification result = notificationService.markAsRead(notificationId, recipientId);

        assertThat(result.isRead()).isTrue();
        assertThat(result.getReadAt()).isNotNull();
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_alreadyRead_doesNotOverwriteOriginalReadAt() {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setRead(true);
        Instant originalReadAt = Instant.parse("2026-01-01T00:00:00Z");
        notification.setReadAt(originalReadAt);
        UUID notificationId = UUID.randomUUID();
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        Notification result = notificationService.markAsRead(notificationId, recipientId);

        assertThat(result.getReadAt()).isEqualTo(originalReadAt);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAsRead_notFound_throwsNotificationNotFoundException() {
        UUID notificationId = UUID.randomUUID();
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class,
                () -> notificationService.markAsRead(notificationId, recipientId));
    }

    @Test
    void markAsRead_belongsToAnotherUser_throwsNotificationAccessDeniedException() {
        Notification notification = new Notification();
        notification.setRecipientId(UUID.randomUUID());
        UUID notificationId = UUID.randomUUID();
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        assertThrows(NotificationAccessDeniedException.class,
                () -> notificationService.markAsRead(notificationId, recipientId));
    }

    @Test
    void countUnread_delegatesToRepository() {
        when(notificationRepository.countByRecipientIdAndReadFalse(recipientId)).thenReturn(3L);

        assertThat(notificationService.countUnread(recipientId)).isEqualTo(3L);
    }

    @Test
    void markAllAsRead_delegatesToRepositoryBulkUpdate() {
        notificationService.markAllAsRead(recipientId);

        verify(notificationRepository).markAllAsRead(eq(recipientId), any(Instant.class));
    }
}
