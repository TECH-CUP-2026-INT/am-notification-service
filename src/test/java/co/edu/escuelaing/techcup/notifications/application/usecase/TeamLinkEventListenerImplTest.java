package co.edu.escuelaing.techcup.notifications.application.usecase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamInvitationEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamLinkRequestedEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamLinkRespondedEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CreateNotificationCommand;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.NotificationUseCase;
import co.edu.escuelaing.techcup.notifications.domain.service.TeamLinkNotificationComposer;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * La lógica de composición del mensaje vive y se prueba en
 * domain/service/TeamLinkNotificationComposerTest; aquí solo se verifica que el
 * listener delega correctamente en el composer y en el caso de uso.
 */
@ExtendWith(MockitoExtension.class)
class TeamLinkEventListenerImplTest {

    @Mock
    private TeamLinkNotificationComposer teamLinkNotificationComposer;

    @Mock
    private NotificationUseCase notificationUseCase;

    private TeamLinkEventListenerImpl listener;

    @BeforeEach
    void setUp() {
        listener = new TeamLinkEventListenerImpl(teamLinkNotificationComposer, notificationUseCase);
    }

    @Test
    void onLinkRequested_delegatesComposedCommandToUseCase() {
        TeamLinkRequestedEvent event = new TeamLinkRequestedEvent(
                UUID.randomUUID(), "Halcones FC", UUID.randomUUID(), "Camilo",
                UUID.randomUUID(), UUID.randomUUID(), Instant.now());
        CreateNotificationCommand composed = new CreateNotificationCommand(
                event.recipientId(), NotificationType.SOLICITUD_VINCULACION_EQUIPO, "Camilo solicitó vincularse.",
                event.requestId());
        when(teamLinkNotificationComposer.composeLinkRequested(event)).thenReturn(composed);

        listener.onLinkRequested(event);

        verify(notificationUseCase).create(composed);
    }

    @Test
    void onLinkResponded_delegatesComposedCommandToUseCase() {
        TeamLinkRespondedEvent event = new TeamLinkRespondedEvent(
                UUID.randomUUID(), "Halcones FC", UUID.randomUUID(), UUID.randomUUID(), true, Instant.now());
        CreateNotificationCommand composed = new CreateNotificationCommand(
                event.recipientId(), NotificationType.VINCULACION_ACEPTADA, "Tu solicitud fue aceptada.", event.teamId());
        when(teamLinkNotificationComposer.composeLinkResponded(event)).thenReturn(composed);

        listener.onLinkResponded(event);

        verify(notificationUseCase).create(composed);
    }

    @Test
    void onTeamInvited_delegatesComposedCommandToUseCase() {
        TeamInvitationEvent event = new TeamInvitationEvent(
                UUID.randomUUID(), "Halcones FC", UUID.randomUUID(), UUID.randomUUID(), "Camilo", Instant.now());
        CreateNotificationCommand composed = new CreateNotificationCommand(
                event.invitedUserId(), NotificationType.INVITACION_EQUIPO, "Camilo te invitó.", event.invitationId());
        when(teamLinkNotificationComposer.composeTeamInvited(event)).thenReturn(composed);

        listener.onTeamInvited(event);

        verify(notificationUseCase).create(composed);
    }
}
