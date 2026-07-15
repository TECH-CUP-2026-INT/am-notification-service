package co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.consumer;

import co.edu.escuelaing.techcup.notifications.domain.ports.in.ChatEventListener;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.config.RabbitMqConfig;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.dto.ChatMessageMessageV1;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Segunda puerta de entrada (junto a ChatEventController) hacia el mismo puerto de dominio. */
@Component
@RequiredArgsConstructor
public class ChatEventConsumer {

    private final ChatEventListener chatEventListener;

    @RabbitListener(queues = RabbitMqConfig.QUEUE_MENSAJES)
    public void onMessage(ChatMessageMessageV1 message) {
        chatEventListener.onNewChatMessage(message.toDomain());
    }
}
