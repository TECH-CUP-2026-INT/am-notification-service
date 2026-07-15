package co.edu.escuelaing.techcup.notifications.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Modelo de dominio de una notificación. No conoce Mongo ni ningún detalle de
 * persistencia; ver infrastructure/out/persistence/mongo/NotificationDocument para el
 * modelo de persistencia equivalente.
 */
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    private UUID id = UUID.randomUUID();

    private UUID recipientId;

    private NotificationType type;

    private String message;

    private UUID referenceId;

    private boolean read;

    private Instant createdAt;

    private Instant readAt;
}
