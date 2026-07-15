package co.edu.escuelaing.techcup.notifications.application.usecase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentProofReceivedEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentStatus;
import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentStatusChangedEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CreateNotificationCommand;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.NotificationUseCase;
import co.edu.escuelaing.techcup.notifications.domain.service.EnrollmentNotificationComposer;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * La lógica de composición del mensaje vive y se prueba en
 * domain/service/EnrollmentNotificationComposerTest; aquí solo se verifica que el
 * listener delega correctamente en el composer y en el caso de uso.
 */
@ExtendWith(MockitoExtension.class)
class EnrollmentEventListenerImplTest {

    @Mock
    private EnrollmentNotificationComposer enrollmentNotificationComposer;

    @Mock
    private NotificationUseCase notificationUseCase;

    private EnrollmentEventListenerImpl listener;

    @BeforeEach
    void setUp() {
        listener = new EnrollmentEventListenerImpl(enrollmentNotificationComposer, notificationUseCase);
    }

    @Test
    void onStatusChanged_delegatesComposedCommandToUseCase() {
        EnrollmentStatusChangedEvent event = new EnrollmentStatusChangedEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), EnrollmentStatus.APROBADA, null, Instant.now());
        CreateNotificationCommand composed = new CreateNotificationCommand(
                event.recipientId(), NotificationType.INSCRIPCION_APROBADA, "Tu inscripción fue aprobada.", event.enrollmentId());
        when(enrollmentNotificationComposer.composeStatusChanged(event)).thenReturn(composed);

        listener.onStatusChanged(event);

        verify(notificationUseCase).create(composed);
    }

    @Test
    void onProofReceived_delegatesComposedCommandToUseCase() {
        EnrollmentProofReceivedEvent event = new EnrollmentProofReceivedEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "https://storage/comprobante.pdf", Instant.now());
        CreateNotificationCommand composed = new CreateNotificationCommand(
                event.recipientId(), NotificationType.INSCRIPCION_COMPROBANTE_RECIBIDO, "Recibimos el comprobante.",
                event.enrollmentId());
        when(enrollmentNotificationComposer.composeProofReceived(event)).thenReturn(composed);

        listener.onProofReceived(event);

        verify(notificationUseCase).create(composed);
    }
}
