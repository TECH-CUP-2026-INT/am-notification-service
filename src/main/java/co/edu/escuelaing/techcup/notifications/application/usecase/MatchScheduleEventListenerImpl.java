package co.edu.escuelaing.techcup.notifications.application.usecase;

import co.edu.escuelaing.techcup.notifications.domain.model.event.MatchScheduleEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.MatchScheduleEventListener;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.NotificationUseCase;
import co.edu.escuelaing.techcup.notifications.domain.service.MatchScheduleNotificationComposer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchScheduleEventListenerImpl implements MatchScheduleEventListener {

    private final MatchScheduleNotificationComposer matchScheduleNotificationComposer;
    private final NotificationUseCase notificationUseCase;

    @Override
    public void onScheduleChanged(MatchScheduleEvent event) {
        notificationUseCase.create(matchScheduleNotificationComposer.compose(event));
    }
}
