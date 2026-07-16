package co.edu.escuelaing.techcup.notifications.email;

import co.edu.escuelaing.techcup.notifications.entity.Notification;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Dispara el correo de una notificación ya persistida. Corre en un hilo aparte
 * ({@code @Async}, ver AsyncConfig) para que una falla o una demora del SMTP nunca
 * retrasen ni tumben la creación de la notificación in-app, que ya quedó guardada antes
 * de llegar acá.
 */
@Component
public class NotificationEmailNotifier {

    private static final Logger log = LoggerFactory.getLogger(NotificationEmailNotifier.class);

    private final RecipientEmailResolver emailResolver;
    private final EmailSenderPort emailSender;

    public NotificationEmailNotifier(RecipientEmailResolver emailResolver, EmailSenderPort emailSender) {
        this.emailResolver = emailResolver;
        this.emailSender = emailSender;
    }

    @Async
    public void notifyByEmail(Notification notification) {
        Optional<String> email = emailResolver.resolveEmail(notification.getRecipientId());
        if (email.isEmpty()) {
            log.debug("Sin correo resuelto para el destinatario {}; se omite el email de la notificación {}",
                    notification.getRecipientId(), notification.getId());
            return;
        }

        try {
            String subject = NotificationEmailTemplates.subjectFor(notification.getType());
            String body = NotificationEmailTemplates.bodyFor(subject, notification.getMessage());
            emailSender.send(email.get(), subject, body);
        } catch (Exception ex) {
            log.warn("No fue posible enviar el correo de la notificación {} a {}: {}",
                    notification.getId(), email.get(), ex.getMessage());
        }
    }
}
