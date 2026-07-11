package co.edu.escuelaing.techcup.notifications.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import co.edu.escuelaing.techcup.notifications.dto.event.EnrollmentProofReceivedEvent;
import co.edu.escuelaing.techcup.notifications.dto.event.EnrollmentStatus;
import co.edu.escuelaing.techcup.notifications.dto.event.EnrollmentStatusChangedEvent;
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
class EnrollmentEventListenerImplTest {

    @Mock
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<CreateNotificationCommand> commandCaptor;

    private EnrollmentEventListenerImpl listener;

    private final UUID enrollmentId = UUID.randomUUID();
    private final UUID recipientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        listener = new EnrollmentEventListenerImpl(notificationService);
    }

    @Test
    void approved_buildsApprovedNotification() {
        EnrollmentStatusChangedEvent event = new EnrollmentStatusChangedEvent(
                enrollmentId, UUID.randomUUID(), recipientId, EnrollmentStatus.APROBADA, null, Instant.now());

        listener.onStatusChanged(event);

        verify(notificationService).create(commandCaptor.capture());
        CreateNotificationCommand command = commandCaptor.getValue();
        assertThat(command.recipientId()).isEqualTo(recipientId);
        assertThat(command.type()).isEqualTo(NotificationType.INSCRIPCION_APROBADA);
        assertThat(command.referenceId()).isEqualTo(enrollmentId);
    }

    @Test
    void rejected_withReason_includesReasonInMessage() {
        EnrollmentStatusChangedEvent event = new EnrollmentStatusChangedEvent(
                enrollmentId, UUID.randomUUID(), recipientId, EnrollmentStatus.RECHAZADA,
                "documentación incompleta", Instant.now());

        listener.onStatusChanged(event);

        verify(notificationService).create(commandCaptor.capture());
        CreateNotificationCommand command = commandCaptor.getValue();
        assertThat(command.type()).isEqualTo(NotificationType.INSCRIPCION_RECHAZADA);
        assertThat(command.message()).contains("documentación incompleta");
    }

    @Test
    void cancelled_buildsCancelledNotification() {
        EnrollmentStatusChangedEvent event = new EnrollmentStatusChangedEvent(
                enrollmentId, UUID.randomUUID(), recipientId, EnrollmentStatus.CANCELADA, null, Instant.now());

        listener.onStatusChanged(event);

        verify(notificationService).create(commandCaptor.capture());
        assertThat(commandCaptor.getValue().type()).isEqualTo(NotificationType.INSCRIPCION_CANCELADA);
    }

    @Test
    void proofReceived_buildsPendingReviewNotification() {
        EnrollmentProofReceivedEvent event = new EnrollmentProofReceivedEvent(
                enrollmentId, UUID.randomUUID(), recipientId, "https://storage/comprobante.pdf", Instant.now());

        listener.onProofReceived(event);

        verify(notificationService).create(commandCaptor.capture());
        CreateNotificationCommand command = commandCaptor.getValue();
        assertThat(command.type()).isEqualTo(NotificationType.INSCRIPCION_COMPROBANTE_RECIBIDO);
        assertThat(command.referenceId()).isEqualTo(enrollmentId);
        assertThat(command.message()).containsIgnoringCase("pendiente");
    }
}
