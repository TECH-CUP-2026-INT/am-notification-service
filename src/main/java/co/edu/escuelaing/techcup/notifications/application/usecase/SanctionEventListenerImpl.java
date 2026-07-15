package co.edu.escuelaing.techcup.notifications.application.usecase;

import co.edu.escuelaing.techcup.notifications.domain.model.event.PlayerSanctionedEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.NotificationUseCase;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.SanctionEventListener;
import co.edu.escuelaing.techcup.notifications.domain.service.SanctionNotificationComposer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SanctionEventListenerImpl implements SanctionEventListener {

    private final SanctionNotificationComposer sanctionNotificationComposer;
    private final NotificationUseCase notificationUseCase;

    @Override
    public void onPlayerSanctioned(PlayerSanctionedEvent event) {
        notificationUseCase.create(sanctionNotificationComposer.compose(event));
    }
}
