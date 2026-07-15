package co.edu.escuelaing.techcup.notifications.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.MatchScheduleAction;
import co.edu.escuelaing.techcup.notifications.domain.model.event.MatchScheduleEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CreateNotificationCommand;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MatchScheduleNotificationComposerTest {

    private final MatchScheduleNotificationComposer composer = new MatchScheduleNotificationComposer();

    private final UUID matchId = UUID.randomUUID();
    private final UUID recipientId = UUID.randomUUID();

    @Test
    void compose_scheduled_buildsScheduledNotificationWithMatchAsReference() {
        MatchScheduleEvent event = new MatchScheduleEvent(
                matchId, UUID.randomUUID(), UUID.randomUUID(), recipientId,
                MatchScheduleAction.PROGRAMADO, Instant.parse("2026-08-01T20:00:00Z"), null, Instant.now());

        CreateNotificationCommand command = composer.compose(event);

        assertThat(command.recipientId()).isEqualTo(recipientId);
        assertThat(command.type()).isEqualTo(NotificationType.PARTIDO_PROGRAMADO);
        assertThat(command.referenceId()).isEqualTo(matchId);
    }

    @Test
    void compose_rescheduled_messageMentionsBothDates() {
        MatchScheduleEvent event = new MatchScheduleEvent(
                matchId, UUID.randomUUID(), UUID.randomUUID(), recipientId,
                MatchScheduleAction.REPROGRAMADO,
                Instant.parse("2026-08-05T18:00:00Z"), Instant.parse("2026-08-01T20:00:00Z"), Instant.now());

        CreateNotificationCommand command = composer.compose(event);

        assertThat(command.type()).isEqualTo(NotificationType.PARTIDO_REPROGRAMADO);
        assertThat(command.message()).contains("2026").contains("al");
    }

    @Test
    void compose_cancelled_buildsCancelledNotification() {
        MatchScheduleEvent event = new MatchScheduleEvent(
                matchId, UUID.randomUUID(), UUID.randomUUID(), recipientId,
                MatchScheduleAction.CANCELADO, null, null, Instant.now());

        CreateNotificationCommand command = composer.compose(event);

        assertThat(command.type()).isEqualTo(NotificationType.PARTIDO_CANCELADO);
    }
}
