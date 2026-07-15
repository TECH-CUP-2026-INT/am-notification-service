package co.edu.escuelaing.techcup.notifications.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.CardType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.PlayerSanctionedEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CreateNotificationCommand;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.NotificationUseCase;
import co.edu.escuelaing.techcup.notifications.domain.service.SanctionNotificationComposer;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * La lógica de composición del mensaje vive y se prueba en
 * domain/service/SanctionNotificationComposerTest; aquí solo se verifica que el
 * listener delega correctamente en el composer y en el caso de uso.
 */
@ExtendWith(MockitoExtension.class)
class SanctionEventListenerImplTest {

    @Mock
    private SanctionNotificationComposer sanctionNotificationComposer;

    @Mock
    private NotificationUseCase notificationUseCase;

    private SanctionEventListenerImpl listener;

    @BeforeEach
    void setUp() {
        listener = new SanctionEventListenerImpl(sanctionNotificationComposer, notificationUseCase);
    }

    @Test
    void onPlayerSanctioned_delegatesComposedCommandToUseCase() {
        PlayerSanctionedEvent event = new PlayerSanctionedEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), CardType.RED, 0, Instant.now());
        CreateNotificationCommand composed = new CreateNotificationCommand(
                event.playerId(), NotificationType.SANCION_TARJETAS, "Recibiste una tarjeta roja.", event.matchId());
        when(sanctionNotificationComposer.compose(event)).thenReturn(composed);

        listener.onPlayerSanctioned(event);

        verify(notificationUseCase).create(composed);
    }

    @Test
    void onPlayerSanctioned_useCaseThrows_exceptionPropagatesToCaller() {
        PlayerSanctionedEvent event = new PlayerSanctionedEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), CardType.RED, 0, Instant.now());
        CreateNotificationCommand composed = new CreateNotificationCommand(
                event.playerId(), NotificationType.SANCION_TARJETAS, "Recibiste una tarjeta roja.", event.matchId());
        when(sanctionNotificationComposer.compose(event)).thenReturn(composed);
        when(notificationUseCase.create(composed)).thenThrow(new IllegalStateException("fallo de persistencia"));

        assertThatThrownBy(() -> listener.onPlayerSanctioned(event))
                .isInstanceOf(IllegalStateException.class);
    }
}
