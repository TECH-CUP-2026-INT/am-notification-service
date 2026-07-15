package co.edu.escuelaing.techcup.notifications.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentProofReceivedEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentStatus;
import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentStatusChangedEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CreateNotificationCommand;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EnrollmentNotificationComposerTest {

    private final EnrollmentNotificationComposer composer = new EnrollmentNotificationComposer();

    private final UUID enrollmentId = UUID.randomUUID();
    private final UUID recipientId = UUID.randomUUID();

    @Test
    void composeStatusChanged_approved_buildsApprovedNotification() {
        EnrollmentStatusChangedEvent event = new EnrollmentStatusChangedEvent(
                enrollmentId, UUID.randomUUID(), recipientId, EnrollmentStatus.APROBADA, null, Instant.now());

        CreateNotificationCommand command = composer.composeStatusChanged(event);

        assertThat(command.recipientId()).isEqualTo(recipientId);
        assertThat(command.type()).isEqualTo(NotificationType.INSCRIPCION_APROBADA);
        assertThat(command.referenceId()).isEqualTo(enrollmentId);
    }

    @Test
    void composeStatusChanged_rejectedWithReason_includesReasonInMessage() {
        EnrollmentStatusChangedEvent event = new EnrollmentStatusChangedEvent(
                enrollmentId, UUID.randomUUID(), recipientId, EnrollmentStatus.RECHAZADA,
                "documentación incompleta", Instant.now());

        CreateNotificationCommand command = composer.composeStatusChanged(event);

        assertThat(command.type()).isEqualTo(NotificationType.INSCRIPCION_RECHAZADA);
        assertThat(command.message()).contains("documentación incompleta");
    }

    @Test
    void composeStatusChanged_cancelled_buildsCancelledNotification() {
        EnrollmentStatusChangedEvent event = new EnrollmentStatusChangedEvent(
                enrollmentId, UUID.randomUUID(), recipientId, EnrollmentStatus.CANCELADA, null, Instant.now());

        CreateNotificationCommand command = composer.composeStatusChanged(event);

        assertThat(command.type()).isEqualTo(NotificationType.INSCRIPCION_CANCELADA);
    }

    @Test
    void composeProofReceived_buildsPendingReviewNotification() {
        EnrollmentProofReceivedEvent event = new EnrollmentProofReceivedEvent(
                enrollmentId, UUID.randomUUID(), recipientId, "https://storage/comprobante.pdf", Instant.now());

        CreateNotificationCommand command = composer.composeProofReceived(event);

        assertThat(command.type()).isEqualTo(NotificationType.INSCRIPCION_COMPROBANTE_RECIBIDO);
        assertThat(command.referenceId()).isEqualTo(enrollmentId);
        assertThat(command.message()).containsIgnoringCase("pendiente");
    }
}
