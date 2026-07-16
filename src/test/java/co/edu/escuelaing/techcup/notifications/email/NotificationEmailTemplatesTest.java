package co.edu.escuelaing.techcup.notifications.email;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.escuelaing.techcup.notifications.entity.enums.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class NotificationEmailTemplatesTest {

    @ParameterizedTest
    @EnumSource(NotificationType.class)
    void subjectFor_everyNotificationType_returnsANonBlankSubject(NotificationType type) {
        assertThat(NotificationEmailTemplates.subjectFor(type)).isNotBlank();
    }

    @Test
    void bodyFor_embedsSubjectAndMessageAsHtml() {
        String body = NotificationEmailTemplates.bodyFor("Sanción por tarjetas", "Fuiste sancionado.");

        assertThat(body).contains("Sanción por tarjetas").contains("Fuiste sancionado.").contains("<html>");
    }
}
