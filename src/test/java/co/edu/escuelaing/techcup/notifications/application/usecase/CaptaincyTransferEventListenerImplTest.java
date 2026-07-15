package co.edu.escuelaing.techcup.notifications.application.usecase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.CaptaincyTransferEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.CaptaincyTransferInitiator;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CreateNotificationCommand;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.NotificationUseCase;
import co.edu.escuelaing.techcup.notifications.domain.service.CaptaincyTransferNotificationComposer;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * La lógica de composición del mensaje vive y se prueba en
 * domain/service/CaptaincyTransferNotificationComposerTest; aquí solo se verifica que
 * el listener delega correctamente en el composer y en el caso de uso.
 */
@ExtendWith(MockitoExtension.class)
class CaptaincyTransferEventListenerImplTest {

    @Mock
    private CaptaincyTransferNotificationComposer captaincyTransferNotificationComposer;

    @Mock
    private NotificationUseCase notificationUseCase;

    private CaptaincyTransferEventListenerImpl listener;

    @BeforeEach
    void setUp() {
        listener = new CaptaincyTransferEventListenerImpl(captaincyTransferNotificationComposer, notificationUseCase);
    }

    @Test
    void onCaptaincyTransferred_delegatesComposedCommandToUseCase() {
        CaptaincyTransferEvent event = new CaptaincyTransferEvent(
                UUID.randomUUID(), "Halcones FC", UUID.randomUUID(), UUID.randomUUID(),
                CaptaincyTransferInitiator.DELEGATION, Instant.now());
        CreateNotificationCommand composed = new CreateNotificationCommand(
                event.newCaptainId(), NotificationType.CAPITANIA_CEDIDA, "Te delegaron la capitanía.", event.teamId());
        when(captaincyTransferNotificationComposer.compose(event)).thenReturn(composed);

        listener.onCaptaincyTransferred(event);

        verify(notificationUseCase).create(composed);
    }
}
