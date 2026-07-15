package co.edu.escuelaing.techcup.notifications.application.usecase;

import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentProofReceivedEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentStatusChangedEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.EnrollmentEventListener;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.NotificationUseCase;
import co.edu.escuelaing.techcup.notifications.domain.service.EnrollmentNotificationComposer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnrollmentEventListenerImpl implements EnrollmentEventListener {

    private final EnrollmentNotificationComposer enrollmentNotificationComposer;
    private final NotificationUseCase notificationUseCase;

    @Override
    public void onStatusChanged(EnrollmentStatusChangedEvent event) {
        notificationUseCase.create(enrollmentNotificationComposer.composeStatusChanged(event));
    }

    @Override
    public void onProofReceived(EnrollmentProofReceivedEvent event) {
        notificationUseCase.create(enrollmentNotificationComposer.composeProofReceived(event));
    }
}
