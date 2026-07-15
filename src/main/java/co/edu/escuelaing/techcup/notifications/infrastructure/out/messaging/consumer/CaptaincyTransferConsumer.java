package co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.consumer;

import co.edu.escuelaing.techcup.notifications.domain.ports.in.CaptaincyTransferEventListener;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.config.RabbitMqConfig;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.dto.CaptaincyTransferMessageV1;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Segunda puerta de entrada (junto a TeamEventController) hacia el mismo puerto de dominio. */
@Component
@RequiredArgsConstructor
public class CaptaincyTransferConsumer {

    private final CaptaincyTransferEventListener captaincyTransferEventListener;

    @RabbitListener(queues = RabbitMqConfig.QUEUE_EQUIPOS_CAPITANIA)
    public void onMessage(CaptaincyTransferMessageV1 message) {
        captaincyTransferEventListener.onCaptaincyTransferred(message.toDomain());
    }
}
