package co.edu.escuelaing.techcup.notifications.listener;

import co.edu.escuelaing.techcup.notifications.dto.event.ChatMessageEvent;
import co.edu.escuelaing.techcup.notifications.entity.enums.NotificationType;
import co.edu.escuelaing.techcup.notifications.service.CreateNotificationCommand;
import co.edu.escuelaing.techcup.notifications.service.NotificationService;
import org.springframework.stereotype.Component;

@Component
public class ChatEventListenerImpl implements ChatEventListener {

    private final NotificationService notificationService;

    public ChatEventListenerImpl(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void onNewChatMessage(ChatMessageEvent event) {
        String message = event.senderName() + " te envió un mensaje: \"" + event.messagePreview() + "\"";

        notificationService.create(new CreateNotificationCommand(
                event.recipientId(), NotificationType.NUEVO_MENSAJE_CHAT, message, event.chatId()));
    }
}
