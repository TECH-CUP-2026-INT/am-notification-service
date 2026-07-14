package co.edu.escuelaing.techcup.notifications.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import co.edu.escuelaing.techcup.notifications.dto.event.CardType;
import co.edu.escuelaing.techcup.notifications.dto.event.PlayerSanctionedEvent;
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
class SanctionEventListenerImplTest {

    @Mock
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<CreateNotificationCommand> commandCaptor;

    private SanctionEventListenerImpl listener;

    @BeforeEach
    void setUp() {
        listener = new SanctionEventListenerImpl(notificationService);
    }

    @Test
    void redCard_notifiesPlayerAsRecipientWithMatchAsReference() {
        UUID matchId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        PlayerSanctionedEvent event = new PlayerSanctionedEvent(
                matchId, UUID.randomUUID(), playerId, CardType.RED, 0, Instant.now());

        listener.onPlayerSanctioned(event);

        verify(notificationService).create(commandCaptor.capture());
        CreateNotificationCommand command = commandCaptor.getValue();
        assertThat(command.recipientId()).isEqualTo(playerId);
        assertThat(command.type()).isEqualTo(NotificationType.SANCION_TARJETAS);
        assertThat(command.referenceId()).isEqualTo(matchId);
        assertThat(command.message()).containsIgnoringCase("roja");
    }

    @Test
    void secondYellowCard_messageMentionsAccumulatedCount() {
        UUID playerId = UUID.randomUUID();
        PlayerSanctionedEvent event = new PlayerSanctionedEvent(
                UUID.randomUUID(), UUID.randomUUID(), playerId, CardType.YELLOW, 2, Instant.now());

        listener.onPlayerSanctioned(event);

        verify(notificationService).create(commandCaptor.capture());
        CreateNotificationCommand command = commandCaptor.getValue();
        assertThat(command.type()).isEqualTo(NotificationType.SANCION_TARJETAS);
        assertThat(command.message()).contains("2");
    }
}
