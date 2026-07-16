package co.edu.escuelaing.techcup.notifications.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import co.edu.escuelaing.techcup.notifications.dto.event.PlayerConductSanctionedEvent;
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
class ConductSanctionEventListenerImplTest {

    @Mock
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<CreateNotificationCommand> commandCaptor;

    private ConductSanctionEventListenerImpl listener;

    @BeforeEach
    void setUp() {
        listener = new ConductSanctionEventListenerImpl(notificationService);
    }

    @Test
    void onPlayerConductSanctioned_notifiesPlayerWithReasonAndMatchCount() {
        UUID playerId = UUID.randomUUID();
        PlayerConductSanctionedEvent event = new PlayerConductSanctionedEvent(
                playerId.toString(), 3, "Agresión a un árbitro", Instant.now());

        listener.onPlayerConductSanctioned(event);

        verify(notificationService).create(commandCaptor.capture());
        CreateNotificationCommand command = commandCaptor.getValue();
        assertThat(command.recipientId()).isEqualTo(playerId);
        assertThat(command.type()).isEqualTo(NotificationType.SANCION_CONDUCTA);
        assertThat(command.referenceId()).isNull();
        assertThat(command.message()).contains("3 partidos").contains("Agresión a un árbitro");
    }

    @Test
    void onPlayerConductSanctioned_singleMatch_messageUsesSingular() {
        UUID playerId = UUID.randomUUID();
        PlayerConductSanctionedEvent event = new PlayerConductSanctionedEvent(
                playerId.toString(), 1, "Conducta antideportiva", Instant.now());

        listener.onPlayerConductSanctioned(event);

        verify(notificationService).create(commandCaptor.capture());
        assertThat(commandCaptor.getValue().message()).contains("1 partido");
    }
}
