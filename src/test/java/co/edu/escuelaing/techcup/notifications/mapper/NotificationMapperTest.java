package co.edu.escuelaing.techcup.notifications.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.escuelaing.techcup.notifications.dto.response.NotificationResponse;
import co.edu.escuelaing.techcup.notifications.entity.Notification;
import co.edu.escuelaing.techcup.notifications.entity.enums.NotificationType;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificationMapperTest {

    @Test
    void toResponse_mapsAllFieldsFromEntity() {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setType(NotificationType.NUEVO_MENSAJE_CHAT);
        notification.setMessage("hola");
        notification.setReferenceId(UUID.randomUUID());
        notification.setRead(true);
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant readAt = Instant.parse("2026-01-02T00:00:00Z");
        notification.setCreatedAt(createdAt);
        notification.setReadAt(readAt);

        NotificationResponse response = NotificationMapper.toResponse(notification);

        assertThat(response.id()).isEqualTo(notification.getId());
        assertThat(response.type()).isEqualTo(NotificationType.NUEVO_MENSAJE_CHAT);
        assertThat(response.message()).isEqualTo("hola");
        assertThat(response.referenceId()).isEqualTo(notification.getReferenceId());
        assertThat(response.read()).isTrue();
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.readAt()).isEqualTo(readAt);
    }

    @Test
    void toResponse_unreadNotification_readAtIsNull() {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setType(NotificationType.NUEVO_MENSAJE_CHAT);
        notification.setMessage("hola");
        notification.setRead(false);
        notification.setCreatedAt(Instant.now());

        NotificationResponse response = NotificationMapper.toResponse(notification);

        assertThat(response.read()).isFalse();
        assertThat(response.readAt()).isNull();
    }
}
