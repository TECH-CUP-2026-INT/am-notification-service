package co.edu.escuelaing.techcup.notifications.domain.ports.out;

import co.edu.escuelaing.techcup.notifications.domain.model.Notification;

/**
 * Puerto de salida para publicar hacia consumidores externos (hoy: el servicio de
 * Estadísticas) que cada notificación creada existe, sin acoplar el caso de uso a
 * RabbitMQ ni a ningún broker concreto.
 */
public interface NotificationEventPublisherPort {

    void publishNotificationCreated(Notification notification);
}
