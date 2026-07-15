package co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.consumer;

import co.edu.escuelaing.techcup.notifications.domain.ports.in.TeamLinkEventListener;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.config.RabbitMqConfig;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.dto.TeamLinkRespondedMessageV1;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Segunda puerta de entrada (junto a TeamEventController) hacia el mismo puerto de dominio. */
@Component
@RequiredArgsConstructor
public class TeamLinkRespondedConsumer {

    private final TeamLinkEventListener teamLinkEventListener;

    @RabbitListener(queues = RabbitMqConfig.QUEUE_EQUIPOS_RESPUESTAS)
    public void onMessage(TeamLinkRespondedMessageV1 message) {
        teamLinkEventListener.onLinkResponded(message.toDomain());
    }
}
