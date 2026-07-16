package co.edu.escuelaing.techcup.notifications.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.escuelaing.techcup.notifications.config.EmailProperties;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

class JavaMailEmailSenderTest {

    private final EmailProperties properties = new EmailProperties("no-reply@techcup.com", null);

    @Test
    void send_buildsAndSendsAMimeMessageWithTheExpectedFields() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        JavaMailEmailSender sender = new JavaMailEmailSender(mailSender, properties);

        sender.send("dev@techcup.com", "Sanción por tarjetas", "<html><body>hola</body></html>");

        verify(mailSender).send(mimeMessage);
        assertThat(mimeMessage.getSubject()).isEqualTo("Sanción por tarjetas");
        assertThat(mimeMessage.getAllRecipients()[0]).hasToString("dev@techcup.com");
        assertThat(mimeMessage.getFrom()[0]).hasToString("no-reply@techcup.com");
    }

    @Test
    void send_whenTheMailSenderFails_wrapsTheFailureInEmailDeliveryException() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new org.springframework.mail.MailSendException("connection refused"))
                .when(mailSender).send(any(MimeMessage.class));
        JavaMailEmailSender sender = new JavaMailEmailSender(mailSender, properties);

        assertThatThrownBy(() -> sender.send("dev@techcup.com", "asunto", "<html></html>"))
                .isInstanceOf(EmailDeliveryException.class);
    }
}
