package co.edu.escuelaing.techcup.notifications.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import co.edu.escuelaing.techcup.notifications.dto.event.TeamInvitationEvent;
import co.edu.escuelaing.techcup.notifications.dto.event.TeamLinkRequestEvent;
import co.edu.escuelaing.techcup.notifications.dto.event.TeamLinkResponseEvent;
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
class TeamLinkEventListenerImplTest {

    @Mock
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<CreateNotificationCommand> commandCaptor;

    private TeamLinkEventListenerImpl listener;

    @BeforeEach
    void setUp() {
        listener = new TeamLinkEventListenerImpl(notificationService);
    }

    @Test
    void linkRequested_notifiesRecipientWithRequestAsReference() {
        UUID requestId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        TeamLinkRequestEvent event = new TeamLinkRequestEvent(
                UUID.randomUUID(), "Halcones FC", UUID.randomUUID(), "Camilo",
                recipientId, requestId, Instant.now());

        listener.onLinkRequested(event);

        verify(notificationService).create(commandCaptor.capture());
        CreateNotificationCommand command = commandCaptor.getValue();
        assertThat(command.recipientId()).isEqualTo(recipientId);
        assertThat(command.type()).isEqualTo(NotificationType.SOLICITUD_VINCULACION_EQUIPO);
        assertThat(command.referenceId()).isEqualTo(requestId);
        assertThat(command.message()).contains("Camilo").contains("Halcones FC");
    }

    @Test
    void linkAccepted_buildsAcceptedNotification() {
        UUID teamId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        TeamLinkResponseEvent event = new TeamLinkResponseEvent(
                teamId, "Halcones FC", UUID.randomUUID(), recipientId, true, Instant.now());

        listener.onLinkResponded(event);

        verify(notificationService).create(commandCaptor.capture());
        CreateNotificationCommand command = commandCaptor.getValue();
        assertThat(command.type()).isEqualTo(NotificationType.VINCULACION_ACEPTADA);
        assertThat(command.referenceId()).isEqualTo(teamId);
        assertThat(command.message()).containsIgnoringCase("aceptada");
    }

    @Test
    void linkRejected_buildsRejectedNotification() {
        TeamLinkResponseEvent event = new TeamLinkResponseEvent(
                UUID.randomUUID(), "Halcones FC", UUID.randomUUID(), UUID.randomUUID(), false, Instant.now());

        listener.onLinkResponded(event);

        verify(notificationService).create(commandCaptor.capture());
        assertThat(commandCaptor.getValue().type()).isEqualTo(NotificationType.VINCULACION_RECHAZADA);
        assertThat(commandCaptor.getValue().message()).containsIgnoringCase("rechazada");
    }

    @Test
    void teamInvited_notifiesInvitedUserWithInvitationAsReference() {
        UUID invitedUserId = UUID.randomUUID();
        UUID invitationId = UUID.randomUUID();
        TeamInvitationEvent event = new TeamInvitationEvent(
                UUID.randomUUID(), "Halcones FC", invitedUserId, invitationId, "Camilo", Instant.now());

        listener.onTeamInvited(event);

        verify(notificationService).create(commandCaptor.capture());
        CreateNotificationCommand command = commandCaptor.getValue();
        assertThat(command.recipientId()).isEqualTo(invitedUserId);
        assertThat(command.type()).isEqualTo(NotificationType.INVITACION_EQUIPO);
        assertThat(command.referenceId()).isEqualTo(invitationId);
    }
}
