package co.edu.escuelaing.techcup.notifications.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import co.edu.escuelaing.techcup.notifications.dto.event.ChatMessageEvent;
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
class ChatEventListenerImplTest {

    @Mock
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<CreateNotificationCommand> commandCaptor;

    private ChatEventListenerImpl listener;

    @BeforeEach
    void setUp() {
        listener = new ChatEventListenerImpl(notificationService);
    }

    @Test
    void newChatMessage_notifiesRecipientWithChatAsReference() {
        UUID chatId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        ChatMessageEvent event = new ChatMessageEvent(
                chatId, UUID.randomUUID(), "Ana", recipientId, "¿Listos para el partido?", Instant.now());

        listener.onNewChatMessage(event);

        verify(notificationService).create(commandCaptor.capture());
        CreateNotificationCommand command = commandCaptor.getValue();
        assertThat(command.recipientId()).isEqualTo(recipientId);
        assertThat(command.type()).isEqualTo(NotificationType.NUEVO_MENSAJE_CHAT);
        assertThat(command.referenceId()).isEqualTo(chatId);
        assertThat(command.message()).contains("Ana").contains("¿Listos para el partido?");
    }
}
