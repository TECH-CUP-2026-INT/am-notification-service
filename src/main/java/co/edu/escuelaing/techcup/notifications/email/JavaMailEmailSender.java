package co.edu.escuelaing.techcup.notifications.email;

import co.edu.escuelaing.techcup.notifications.config.EmailProperties;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class JavaMailEmailSender implements EmailSenderPort {

    private final JavaMailSender mailSender;
    private final EmailProperties properties;

    public JavaMailEmailSender(JavaMailSender mailSender, EmailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setFrom(properties.from());
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception ex) {
            throw new EmailDeliveryException("No fue posible enviar el correo a " + to, ex);
        }
    }
}
