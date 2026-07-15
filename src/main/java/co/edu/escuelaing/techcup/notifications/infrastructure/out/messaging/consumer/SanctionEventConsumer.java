package co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.consumer;

import co.edu.escuelaing.techcup.notifications.domain.ports.in.SanctionEventListener;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.config.RabbitMqConfig;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.dto.PlayerSanctionedMessageV1;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Segunda puerta de entrada (junto a SanctionEventController) hacia el mismo puerto de dominio. */
@Component
@RequiredArgsConstructor
public class SanctionEventConsumer {

    private final SanctionEventListener sanctionEventListener;

    @RabbitListener(queues = RabbitMqConfig.QUEUE_SANCIONES)
    public void onMessage(PlayerSanctionedMessageV1 message) {
        sanctionEventListener.onPlayerSanctioned(message.toDomain());
    }
}
