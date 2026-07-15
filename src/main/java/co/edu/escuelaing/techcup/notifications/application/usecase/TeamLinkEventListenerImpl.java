package co.edu.escuelaing.techcup.notifications.application.usecase;

import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamInvitationEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamLinkRequestedEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamLinkRespondedEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.NotificationUseCase;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.TeamLinkEventListener;
import co.edu.escuelaing.techcup.notifications.domain.service.TeamLinkNotificationComposer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeamLinkEventListenerImpl implements TeamLinkEventListener {

    private final TeamLinkNotificationComposer teamLinkNotificationComposer;
    private final NotificationUseCase notificationUseCase;

    @Override
    public void onLinkRequested(TeamLinkRequestedEvent event) {
        notificationUseCase.create(teamLinkNotificationComposer.composeLinkRequested(event));
    }

    @Override
    public void onLinkResponded(TeamLinkRespondedEvent event) {
        notificationUseCase.create(teamLinkNotificationComposer.composeLinkResponded(event));
    }

    @Override
    public void onTeamInvited(TeamInvitationEvent event) {
        notificationUseCase.create(teamLinkNotificationComposer.composeTeamInvited(event));
    }
}
