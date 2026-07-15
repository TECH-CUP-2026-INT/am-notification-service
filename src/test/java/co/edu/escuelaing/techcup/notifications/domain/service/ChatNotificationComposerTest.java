package co.edu.escuelaing.techcup.notifications.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.ChatMessageEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CreateNotificationCommand;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChatNotificationComposerTest {

    private final ChatNotificationComposer composer = new ChatNotificationComposer();

    @Test
    void compose_notifiesRecipientWithChatAsReference() {
        UUID chatId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        ChatMessageEvent event = new ChatMessageEvent(
                chatId, UUID.randomUUID(), "Ana", recipientId, "¿Listos para el partido?", Instant.now());

        CreateNotificationCommand command = composer.compose(event);

        assertThat(command.recipientId()).isEqualTo(recipientId);
        assertThat(command.type()).isEqualTo(NotificationType.NUEVO_MENSAJE_CHAT);
        assertThat(command.referenceId()).isEqualTo(chatId);
        assertThat(command.message()).contains("Ana").contains("¿Listos para el partido?");
    }
}
