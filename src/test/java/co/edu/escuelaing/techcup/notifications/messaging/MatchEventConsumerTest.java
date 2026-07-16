package co.edu.escuelaing.techcup.notifications.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import co.edu.escuelaing.techcup.notifications.entity.enums.NotificationType;
import co.edu.escuelaing.techcup.notifications.service.CreateNotificationCommand;
import co.edu.escuelaing.techcup.notifications.service.NotificationService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MatchEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    private MatchEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new MatchEventConsumer(notificationService);
    }

    @Test
    void onMatchEvent_won_notifiesThePlayerAsRecipientWithMatchAsReference() {
        UUID playerId = UUID.randomUUID();
        UUID matchId = UUID.randomUUID();
        MatchStatEvent event = new MatchStatEvent(playerId.toString(), UUID.randomUUID().toString(),
                matchId.toString(), null, "WON", 2, 1, 0, 0, 0, 0, false, LocalDateTime.now());

        consumer.onMatchEvent(event);

        ArgumentCaptor<CreateNotificationCommand> captor = ArgumentCaptor.forClass(CreateNotificationCommand.class);
        verify(notificationService).create(captor.capture());
        CreateNotificationCommand command = captor.getValue();
        assertThat(command.recipientId()).isEqualTo(playerId);
        assertThat(command.type()).isEqualTo(NotificationType.RESULTADO_PARTIDO);
        assertThat(command.referenceId()).isEqualTo(matchId);
        assertThat(command.message()).contains("ganó").contains("2 goles").contains("tarjeta amarilla");
    }

    @Test
    void onMatchEvent_lostWithNoGoalsOrCards_messageOnlyMentionsResult() {
        MatchStatEvent event = new MatchStatEvent(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                UUID.randomUUID().toString(), null, "LOST", 0, 0, 0, 0, 0, 0, false, LocalDateTime.now());

        consumer.onMatchEvent(event);

        ArgumentCaptor<CreateNotificationCommand> captor = ArgumentCaptor.forClass(CreateNotificationCommand.class);
        verify(notificationService).create(captor.capture());
        assertThat(captor.getValue().message()).isEqualTo("Tu equipo perdió el partido.");
    }

    @Test
    void onMatchEvent_redCard_takesPrecedenceOverYellowInTheMessage() {
        MatchStatEvent event = new MatchStatEvent(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                UUID.randomUUID().toString(), null, "DRAWN", 0, 1, 1, 0, 0, 0, false, LocalDateTime.now());

        consumer.onMatchEvent(event);

        ArgumentCaptor<CreateNotificationCommand> captor = ArgumentCaptor.forClass(CreateNotificationCommand.class);
        verify(notificationService).create(captor.capture());
        assertThat(captor.getValue().message()).contains("tarjeta roja").doesNotContain("amarilla");
    }

    @Test
    void onMatchEvent_invalidPlayerId_doesNotPropagateAndDoesNotCreateNotification() {
        MatchStatEvent event = new MatchStatEvent("not-a-uuid", UUID.randomUUID().toString(),
                UUID.randomUUID().toString(), null, "WON", 0, 0, 0, 0, 0, 0, false, LocalDateTime.now());

        assertThatCode(() -> consumer.onMatchEvent(event)).doesNotThrowAnyException();
        verifyNoInteractions(notificationService);
    }
}
