package co.edu.escuelaing.techcup.notifications.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.escuelaing.techcup.notifications.domain.exception.NotificationAccessDeniedException;
import co.edu.escuelaing.techcup.notifications.domain.exception.NotificationNotFoundException;
import co.edu.escuelaing.techcup.notifications.domain.model.Notification;
import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CreateNotificationCommand;
import co.edu.escuelaing.techcup.notifications.domain.ports.out.NotificationEventPublisherPort;
import co.edu.escuelaing.techcup.notifications.domain.ports.out.NotificationRepositoryPort;
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
    private NotificationRepositoryPort notificationRepositoryPort;

    @Mock
    private NotificationEventPublisherPort notificationEventPublisherPort;

    private NotificationServiceImpl notificationService;

    private final UUID recipientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(notificationRepositoryPort, notificationEventPublisherPort);
    }

    @Test
    void create_persistsNotificationWithCommandFieldsAndPublishesEvent() {
        UUID referenceId = UUID.randomUUID();
        CreateNotificationCommand command = new CreateNotificationCommand(
                recipientId, NotificationType.NUEVO_MENSAJE_CHAT, "hola", referenceId);
        when(notificationRepositoryPort.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.create(command);

        assertThat(result.getRecipientId()).isEqualTo(recipientId);
        assertThat(result.getType()).isEqualTo(NotificationType.NUEVO_MENSAJE_CHAT);
        assertThat(result.getMessage()).isEqualTo("hola");
        assertThat(result.getReferenceId()).isEqualTo(referenceId);
        assertThat(result.isRead()).isFalse();
        verify(notificationEventPublisherPort).publishNotificationCreated(result);
    }

    @Test
    void create_missingRecipientId_throwsIllegalStateExceptionAndDoesNotPersist() {
        CreateNotificationCommand command = new CreateNotificationCommand(
                null, NotificationType.NUEVO_MENSAJE_CHAT, "hola", UUID.randomUUID());

        assertThrows(IllegalStateException.class, () -> notificationService.create(command));

        verify(notificationRepositoryPort, never()).save(any());
        verify(notificationEventPublisherPort, never()).publishNotificationCreated(any());
    }

    @Test
    void create_blankMessage_throwsIllegalStateExceptionAndDoesNotPersist() {
        CreateNotificationCommand command = new CreateNotificationCommand(
                recipientId, NotificationType.NUEVO_MENSAJE_CHAT, "   ", UUID.randomUUID());

        assertThrows(IllegalStateException.class, () -> notificationService.create(command));

        verify(notificationRepositoryPort, never()).save(any());
    }

    @Test
    void listForUser_withoutFilter_returnsFullHistoryOrderedByRepository() {
        List<Notification> expected = List.of(new Notification());
        when(notificationRepositoryPort.findByRecipientIdOrderByCreatedAtDesc(recipientId)).thenReturn(expected);

        List<Notification> result = notificationService.listForUser(recipientId, null);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void listForUser_withFilter_delegatesToFilteredQuery() {
        List<Notification> expected = List.of(new Notification());
        when(notificationRepositoryPort.findByRecipientIdAndReadOrderByCreatedAtDesc(recipientId, false))
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
        when(notificationRepositoryPort.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepositoryPort.save(notification)).thenReturn(notification);

        Notification result = notificationService.markAsRead(notificationId, recipientId);

        assertThat(result.isRead()).isTrue();
        assertThat(result.getReadAt()).isNotNull();
        verify(notificationRepositoryPort).save(notification);
    }

    @Test
    void markAsRead_alreadyRead_doesNotOverwriteOriginalReadAt() {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setRead(true);
        Instant originalReadAt = Instant.parse("2026-01-01T00:00:00Z");
        notification.setReadAt(originalReadAt);
        UUID notificationId = UUID.randomUUID();
        when(notificationRepositoryPort.findById(notificationId)).thenReturn(Optional.of(notification));

        Notification result = notificationService.markAsRead(notificationId, recipientId);

        assertThat(result.getReadAt()).isEqualTo(originalReadAt);
        verify(notificationRepositoryPort, never()).save(any());
    }

    @Test
    void markAsRead_notFound_throwsNotificationNotFoundException() {
        UUID notificationId = UUID.randomUUID();
        when(notificationRepositoryPort.findById(notificationId)).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class,
                () -> notificationService.markAsRead(notificationId, recipientId));
    }

    @Test
    void markAsRead_belongsToAnotherUser_throwsNotificationAccessDeniedException() {
        Notification notification = new Notification();
        notification.setRecipientId(UUID.randomUUID());
        UUID notificationId = UUID.randomUUID();
        when(notificationRepositoryPort.findById(notificationId)).thenReturn(Optional.of(notification));

        assertThrows(NotificationAccessDeniedException.class,
                () -> notificationService.markAsRead(notificationId, recipientId));
    }

    @Test
    void countUnread_delegatesToRepository() {
        when(notificationRepositoryPort.countByRecipientIdAndReadFalse(recipientId)).thenReturn(3L);

        assertThat(notificationService.countUnread(recipientId)).isEqualTo(3L);
    }

    @Test
    void markAllAsRead_delegatesToRepositoryBulkUpdate() {
        notificationService.markAllAsRead(recipientId);

        verify(notificationRepositoryPort).markAllAsRead(eq(recipientId), any(Instant.class));
    }
}
