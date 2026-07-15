package co.edu.escuelaing.techcup.notifications.domain.service;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.ChatMessageEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CreateNotificationCommand;
import org.springframework.stereotype.Service;

/** Traduce un ChatMessageEvent al comando de notificación correspondiente. Sin dependencias de framework más allá de la anotación de componente. */
@Service
public class ChatNotificationComposer {

    public CreateNotificationCommand compose(ChatMessageEvent event) {
        String message = event.senderName() + " te envió un mensaje: \"" + event.messagePreview() + "\"";

        return new CreateNotificationCommand(
                event.recipientId(), NotificationType.NUEVO_MENSAJE_CHAT, message, event.chatId());
    }
}
