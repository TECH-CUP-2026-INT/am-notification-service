package co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.consumer;

import co.edu.escuelaing.techcup.notifications.domain.ports.in.EnrollmentEventListener;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.config.RabbitMqConfig;
import co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.dto.EnrollmentProofReceivedMessageV1;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Segunda puerta de entrada (junto a EnrollmentEventController) hacia el mismo puerto de dominio. */
@Component
@RequiredArgsConstructor
public class EnrollmentProofReceivedConsumer {

    private final EnrollmentEventListener enrollmentEventListener;

    @RabbitListener(queues = RabbitMqConfig.QUEUE_INSCRIPCIONES_COMPROBANTE)
    public void onMessage(EnrollmentProofReceivedMessageV1 message) {
        enrollmentEventListener.onProofReceived(message.toDomain());
    }
}
