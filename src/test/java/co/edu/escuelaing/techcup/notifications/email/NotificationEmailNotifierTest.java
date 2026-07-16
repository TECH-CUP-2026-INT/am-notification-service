package co.edu.escuelaing.techcup.notifications.email;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.escuelaing.techcup.notifications.entity.Notification;
import co.edu.escuelaing.techcup.notifications.entity.enums.NotificationType;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationEmailNotifierTest {

    @Mock
    private RecipientEmailResolver emailResolver;

    @Mock
    private EmailSenderPort emailSender;

    private NotificationEmailNotifier notifier;

    private Notification notification() {
        Notification notification = new Notification();
        notification.setRecipientId(UUID.randomUUID());
        notification.setType(NotificationType.SANCION_TARJETAS);
        notification.setMessage("Fuiste sancionado.");
        return notification;
    }

    @BeforeEach
    void setUp() {
        notifier = new NotificationEmailNotifier(emailResolver, emailSender);
    }

    @Test
    void notifyByEmail_withResolvedEmail_sendsIt() {
        Notification notification = notification();
        when(emailResolver.resolveEmail(notification.getRecipientId())).thenReturn(Optional.of("dev@techcup.com"));

        notifier.notifyByEmail(notification);

        verify(emailSender).send("dev@techcup.com", "Sanción por tarjetas", NotificationEmailTemplates
                .bodyFor("Sanción por tarjetas", "Fuiste sancionado."));
    }

    @Test
    void notifyByEmail_withoutResolvedEmail_doesNotSendAnything() {
        Notification notification = notification();
        when(emailResolver.resolveEmail(notification.getRecipientId())).thenReturn(Optional.empty());

        notifier.notifyByEmail(notification);

        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void notifyByEmail_whenSendingFails_doesNotPropagateTheException() {
        Notification notification = notification();
        when(emailResolver.resolveEmail(notification.getRecipientId())).thenReturn(Optional.of("dev@techcup.com"));
        doThrow(new EmailDeliveryException("boom", new RuntimeException()))
                .when(emailSender).send(anyString(), anyString(), anyString());

        assertThatCode(() -> notifier.notifyByEmail(notification)).doesNotThrowAnyException();
    }
}
