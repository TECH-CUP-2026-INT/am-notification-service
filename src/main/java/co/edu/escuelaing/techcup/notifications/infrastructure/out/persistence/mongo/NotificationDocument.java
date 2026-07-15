package co.edu.escuelaing.techcup.notifications.infrastructure.out.persistence.mongo;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** Modelo de persistencia de Mongo. Ver domain/model/Notification para el modelo de dominio. */
@Document(collection = "notification")
@CompoundIndexes({
        @CompoundIndex(name = "ix_notification_recipient_unread", def = "{'recipientId': 1, 'read': 1}"),
        @CompoundIndex(name = "ix_notification_recipient_created", def = "{'recipientId': 1, 'createdAt': -1}")
})
@Getter
@Setter
@NoArgsConstructor
public class NotificationDocument {

    @Id
    private UUID id = UUID.randomUUID();

    @Indexed
    private UUID recipientId;

    private NotificationType type;

    private String message;

    private UUID referenceId;

    private boolean read;

    private Instant createdAt;

    private Instant readAt;
}
