package co.edu.escuelaing.techcup.notifications.application.usecase;

import co.edu.escuelaing.techcup.notifications.domain.model.event.ChatMessageEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.ChatEventListener;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.NotificationUseCase;
import co.edu.escuelaing.techcup.notifications.domain.service.ChatNotificationComposer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatEventListenerImpl implements ChatEventListener {

    private final ChatNotificationComposer chatNotificationComposer;
    private final NotificationUseCase notificationUseCase;

    @Override
    public void onNewChatMessage(ChatMessageEvent event) {
        notificationUseCase.create(chatNotificationComposer.compose(event));
    }
}
