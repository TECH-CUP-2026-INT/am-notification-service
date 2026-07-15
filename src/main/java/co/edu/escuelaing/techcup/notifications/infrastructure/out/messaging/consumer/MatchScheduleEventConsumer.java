package co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.consumer;

import co.edu.escuelaing.techcup.notifications.domain.ports.in.MatchScheduleEventListener;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.config.RabbitMqConfig;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.dto.MatchScheduleMessageV1;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Segunda puerta de entrada (junto a MatchScheduleEventController) hacia el mismo puerto de dominio. */
@Component
@RequiredArgsConstructor
public class MatchScheduleEventConsumer {

    private final MatchScheduleEventListener matchScheduleEventListener;

    @RabbitListener(queues = RabbitMqConfig.QUEUE_PARTIDOS)
    public void onMessage(MatchScheduleMessageV1 message) {
        matchScheduleEventListener.onScheduleChanged(message.toDomain());
    }
}
