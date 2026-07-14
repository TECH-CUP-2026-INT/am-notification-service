package co.edu.escuelaing.techcup.notifications.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import co.edu.escuelaing.techcup.notifications.dto.event.MatchScheduleAction;
import co.edu.escuelaing.techcup.notifications.dto.event.MatchScheduleEvent;
import co.edu.escuelaing.techcup.notifications.entity.enums.NotificationType;
import co.edu.escuelaing.techcup.notifications.service.CreateNotificationCommand;
import co.edu.escuelaing.techcup.notifications.service.NotificationService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MatchScheduleEventListenerImplTest {

    @Mock
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<CreateNotificationCommand> commandCaptor;

    private MatchScheduleEventListenerImpl listener;

    private final UUID matchId = UUID.randomUUID();
    private final UUID recipientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        listener = new MatchScheduleEventListenerImpl(notificationService);
    }

    @Test
    void scheduled_buildsScheduledNotificationWithMatchAsReference() {
        MatchScheduleEvent event = new MatchScheduleEvent(
                matchId, UUID.randomUUID(), UUID.randomUUID(), recipientId,
                MatchScheduleAction.PROGRAMADO, Instant.parse("2026-08-01T20:00:00Z"), null, Instant.now());

        listener.onScheduleChanged(event);

        verify(notificationService).create(commandCaptor.capture());
        CreateNotificationCommand command = commandCaptor.getValue();
        assertThat(command.recipientId()).isEqualTo(recipientId);
        assertThat(command.type()).isEqualTo(NotificationType.PARTIDO_PROGRAMADO);
        assertThat(command.referenceId()).isEqualTo(matchId);
    }

    @Test
    void rescheduled_messageMentionsBothDates() {
        MatchScheduleEvent event = new MatchScheduleEvent(
                matchId, UUID.randomUUID(), UUID.randomUUID(), recipientId,
                MatchScheduleAction.REPROGRAMADO,
                Instant.parse("2026-08-05T18:00:00Z"), Instant.parse("2026-08-01T20:00:00Z"), Instant.now());

        listener.onScheduleChanged(event);

        verify(notificationService).create(commandCaptor.capture());
        CreateNotificationCommand command = commandCaptor.getValue();
        assertThat(command.type()).isEqualTo(NotificationType.PARTIDO_REPROGRAMADO);
        assertThat(command.message()).contains("2026").contains("al");
    }

    @Test
    void cancelled_buildsCancelledNotification() {
        MatchScheduleEvent event = new MatchScheduleEvent(
                matchId, UUID.randomUUID(), UUID.randomUUID(), recipientId,
                MatchScheduleAction.CANCELADO, null, null, Instant.now());

        listener.onScheduleChanged(event);

        verify(notificationService).create(commandCaptor.capture());
        assertThat(commandCaptor.getValue().type()).isEqualTo(NotificationType.PARTIDO_CANCELADO);
    }
}
