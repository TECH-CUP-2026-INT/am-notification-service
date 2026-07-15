package co.edu.escuelaing.techcup.notifications.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.CaptaincyTransferEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.CaptaincyTransferInitiator;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CreateNotificationCommand;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CaptaincyTransferNotificationComposerTest {

    private final CaptaincyTransferNotificationComposer composer = new CaptaincyTransferNotificationComposer();

    @Test
    void compose_delegation_notifiesNewCaptain() {
        UUID teamId = UUID.randomUUID();
        UUID currentCaptainId = UUID.randomUUID();
        UUID newCaptainId = UUID.randomUUID();
        CaptaincyTransferEvent event = new CaptaincyTransferEvent(
                teamId, "Halcones FC", currentCaptainId, newCaptainId,
                CaptaincyTransferInitiator.DELEGATION, Instant.now());

        CreateNotificationCommand command = composer.compose(event);

        assertThat(command.recipientId()).isEqualTo(newCaptainId);
        assertThat(command.type()).isEqualTo(NotificationType.CAPITANIA_CEDIDA);
        assertThat(command.referenceId()).isEqualTo(teamId);
        assertThat(command.message()).contains("Halcones FC");
    }

    @Test
    void compose_application_notifiesCurrentCaptain() {
        UUID teamId = UUID.randomUUID();
        UUID currentCaptainId = UUID.randomUUID();
        UUID newCaptainId = UUID.randomUUID();
        CaptaincyTransferEvent event = new CaptaincyTransferEvent(
                teamId, "Halcones FC", currentCaptainId, newCaptainId,
                CaptaincyTransferInitiator.APPLICATION, Instant.now());

        CreateNotificationCommand command = composer.compose(event);

        assertThat(command.recipientId()).isEqualTo(currentCaptainId);
        assertThat(command.type()).isEqualTo(NotificationType.CAPITANIA_SOLICITADA);
        assertThat(command.referenceId()).isEqualTo(teamId);
        assertThat(command.message()).contains("Halcones FC");
    }
}
