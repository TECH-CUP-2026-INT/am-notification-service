package co.edu.escuelaing.techcup.notifications.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamInvitationEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamLinkRequestedEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamLinkRespondedEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CreateNotificationCommand;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TeamLinkNotificationComposerTest {

    private final TeamLinkNotificationComposer composer = new TeamLinkNotificationComposer();

    @Test
    void composeLinkRequested_notifiesRecipientWithRequestAsReference() {
        UUID requestId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        TeamLinkRequestedEvent event = new TeamLinkRequestedEvent(
                UUID.randomUUID(), "Halcones FC", UUID.randomUUID(), "Camilo",
                recipientId, requestId, Instant.now());

        CreateNotificationCommand command = composer.composeLinkRequested(event);

        assertThat(command.recipientId()).isEqualTo(recipientId);
        assertThat(command.type()).isEqualTo(NotificationType.SOLICITUD_VINCULACION_EQUIPO);
        assertThat(command.referenceId()).isEqualTo(requestId);
        assertThat(command.message()).contains("Camilo").contains("Halcones FC");
    }

    @Test
    void composeLinkResponded_accepted_buildsAcceptedNotification() {
        UUID teamId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        TeamLinkRespondedEvent event = new TeamLinkRespondedEvent(
                teamId, "Halcones FC", UUID.randomUUID(), recipientId, true, Instant.now());

        CreateNotificationCommand command = composer.composeLinkResponded(event);

        assertThat(command.type()).isEqualTo(NotificationType.VINCULACION_ACEPTADA);
        assertThat(command.referenceId()).isEqualTo(teamId);
        assertThat(command.message()).containsIgnoringCase("aceptada");
    }

    @Test
    void composeLinkResponded_rejected_buildsRejectedNotification() {
        TeamLinkRespondedEvent event = new TeamLinkRespondedEvent(
                UUID.randomUUID(), "Halcones FC", UUID.randomUUID(), UUID.randomUUID(), false, Instant.now());

        CreateNotificationCommand command = composer.composeLinkResponded(event);

        assertThat(command.type()).isEqualTo(NotificationType.VINCULACION_RECHAZADA);
        assertThat(command.message()).containsIgnoringCase("rechazada");
    }

    @Test
    void composeTeamInvited_notifiesInvitedUserWithInvitationAsReference() {
        UUID invitedUserId = UUID.randomUUID();
        UUID invitationId = UUID.randomUUID();
        TeamInvitationEvent event = new TeamInvitationEvent(
                UUID.randomUUID(), "Halcones FC", invitedUserId, invitationId, "Camilo", Instant.now());

        CreateNotificationCommand command = composer.composeTeamInvited(event);

        assertThat(command.recipientId()).isEqualTo(invitedUserId);
        assertThat(command.type()).isEqualTo(NotificationType.INVITACION_EQUIPO);
        assertThat(command.referenceId()).isEqualTo(invitationId);
    }
}
