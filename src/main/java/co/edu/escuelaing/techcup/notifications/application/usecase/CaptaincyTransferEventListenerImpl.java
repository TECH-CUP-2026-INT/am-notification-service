package co.edu.escuelaing.techcup.notifications.application.usecase;

import co.edu.escuelaing.techcup.notifications.domain.model.event.CaptaincyTransferEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CaptaincyTransferEventListener;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.NotificationUseCase;
import co.edu.escuelaing.techcup.notifications.domain.service.CaptaincyTransferNotificationComposer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CaptaincyTransferEventListenerImpl implements CaptaincyTransferEventListener {

    private final CaptaincyTransferNotificationComposer captaincyTransferNotificationComposer;
    private final NotificationUseCase notificationUseCase;

    @Override
    public void onCaptaincyTransferred(CaptaincyTransferEvent event) {
        notificationUseCase.create(captaincyTransferNotificationComposer.compose(event));
    }
}
