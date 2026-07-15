package co.edu.escuelaing.techcup.notifications.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.ChatMessageEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CreateNotificationCommand;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.NotificationUseCase;
import co.edu.escuelaing.techcup.notifications.domain.service.ChatNotificationComposer;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * La lógica de composición del mensaje vive y se prueba en
 * domain/service/ChatNotificationComposerTest; aquí solo se verifica que el listener
 * delega correctamente en el composer y en el caso de uso.
 */
@ExtendWith(MockitoExtension.class)
class ChatEventListenerImplTest {

    @Mock
    private ChatNotificationComposer chatNotificationComposer;

    @Mock
    private NotificationUseCase notificationUseCase;

    private ChatEventListenerImpl listener;

    @BeforeEach
    void setUp() {
        listener = new ChatEventListenerImpl(chatNotificationComposer, notificationUseCase);
    }

    @Test
    void onNewChatMessage_delegatesComposedCommandToUseCase() {
        ChatMessageEvent event = new ChatMessageEvent(
                UUID.randomUUID(), UUID.randomUUID(), "Ana", UUID.randomUUID(), "hola equipo", Instant.now());
        CreateNotificationCommand composed = new CreateNotificationCommand(
                event.recipientId(), NotificationType.NUEVO_MENSAJE_CHAT, "Ana te envió un mensaje", event.chatId());
        when(chatNotificationComposer.compose(event)).thenReturn(composed);

        listener.onNewChatMessage(event);

        verify(notificationUseCase).create(composed);
    }

    @Test
    void onNewChatMessage_useCaseThrows_exceptionPropagatesToCaller() {
        ChatMessageEvent event = new ChatMessageEvent(
                UUID.randomUUID(), UUID.randomUUID(), "Ana", UUID.randomUUID(), "hola", Instant.now());
        CreateNotificationCommand composed = new CreateNotificationCommand(
                event.recipientId(), NotificationType.NUEVO_MENSAJE_CHAT, "Ana te envió un mensaje", event.chatId());
        when(chatNotificationComposer.compose(event)).thenReturn(composed);
        when(notificationUseCase.create(composed)).thenThrow(new IllegalStateException("fallo de persistencia"));

        assertThatThrownBy(() -> listener.onNewChatMessage(event))
                .isInstanceOf(IllegalStateException.class);
    }
}
