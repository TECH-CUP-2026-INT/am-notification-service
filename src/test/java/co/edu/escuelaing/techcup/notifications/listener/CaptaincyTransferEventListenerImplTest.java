package co.edu.escuelaing.techcup.notifications.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import co.edu.escuelaing.techcup.notifications.dto.event.CaptaincyTransferEvent;
import co.edu.escuelaing.techcup.notifications.dto.event.CaptaincyTransferInitiator;
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
class CaptaincyTransferEventListenerImplTest {

    @Mock
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<CreateNotificationCommand> commandCaptor;

    private CaptaincyTransferEventListenerImpl listener;

    @BeforeEach
    void setUp() {
        listener = new CaptaincyTransferEventListenerImpl(notificationService);
    }

    @Test
    void delegation_notifiesNewCaptain() {
        UUID teamId = UUID.randomUUID();
        UUID currentCaptainId = UUID.randomUUID();
        UUID newCaptainId = UUID.randomUUID();
        CaptaincyTransferEvent event = new CaptaincyTransferEvent(
                teamId, "Halcones FC", currentCaptainId, newCaptainId,
                CaptaincyTransferInitiator.DELEGATION, Instant.now());

        listener.onCaptaincyTransferred(event);

        verify(notificationService).create(commandCaptor.capture());
        CreateNotificationCommand command = commandCaptor.getValue();
        assertThat(command.recipientId()).isEqualTo(newCaptainId);
        assertThat(command.type()).isEqualTo(NotificationType.CAPITANIA_CEDIDA);
        assertThat(command.referenceId()).isEqualTo(teamId);
        assertThat(command.message()).contains("Halcones FC");
    }

    @Test
    void application_notifiesCurrentCaptain() {
        UUID teamId = UUID.randomUUID();
        UUID currentCaptainId = UUID.randomUUID();
        UUID newCaptainId = UUID.randomUUID();
        CaptaincyTransferEvent event = new CaptaincyTransferEvent(
                teamId, "Halcones FC", currentCaptainId, newCaptainId,
                CaptaincyTransferInitiator.APPLICATION, Instant.now());

        listener.onCaptaincyTransferred(event);

        verify(notificationService).create(commandCaptor.capture());
        CreateNotificationCommand command = commandCaptor.getValue();
        assertThat(command.recipientId()).isEqualTo(currentCaptainId);
        assertThat(command.type()).isEqualTo(NotificationType.CAPITANIA_SOLICITADA);
        assertThat(command.referenceId()).isEqualTo(teamId);
        assertThat(command.message()).contains("Halcones FC");
    }
}
