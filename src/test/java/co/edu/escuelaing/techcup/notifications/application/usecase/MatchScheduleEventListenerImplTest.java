package co.edu.escuelaing.techcup.notifications.application.usecase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.MatchScheduleAction;
import co.edu.escuelaing.techcup.notifications.domain.model.event.MatchScheduleEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CreateNotificationCommand;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.NotificationUseCase;
import co.edu.escuelaing.techcup.notifications.domain.service.MatchScheduleNotificationComposer;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * La lógica de composición del mensaje vive y se prueba en
 * domain/service/MatchScheduleNotificationComposerTest; aquí solo se verifica que el
 * listener delega correctamente en el composer y en el caso de uso.
 */
@ExtendWith(MockitoExtension.class)
class MatchScheduleEventListenerImplTest {

    @Mock
    private MatchScheduleNotificationComposer matchScheduleNotificationComposer;

    @Mock
    private NotificationUseCase notificationUseCase;

    private MatchScheduleEventListenerImpl listener;

    @BeforeEach
    void setUp() {
        listener = new MatchScheduleEventListenerImpl(matchScheduleNotificationComposer, notificationUseCase);
    }

    @Test
    void onScheduleChanged_delegatesComposedCommandToUseCase() {
        MatchScheduleEvent event = new MatchScheduleEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                MatchScheduleAction.PROGRAMADO, Instant.now(), null, Instant.now());
        CreateNotificationCommand composed = new CreateNotificationCommand(
                event.recipientId(), NotificationType.PARTIDO_PROGRAMADO, "Tu partido fue programado.", event.matchId());
        when(matchScheduleNotificationComposer.compose(event)).thenReturn(composed);

        listener.onScheduleChanged(event);

        verify(notificationUseCase).create(composed);
    }
}
