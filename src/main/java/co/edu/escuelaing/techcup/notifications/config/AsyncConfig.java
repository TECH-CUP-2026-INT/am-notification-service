package co.edu.escuelaing.techcup.notifications.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Habilita {@code @Async} para que el envío de correo (NotificationEmailNotifier)
 * corra en un hilo aparte y nunca retrase la respuesta {@code 202 Accepted} de los
 * webhooks (RNF-01: este servicio nunca debe bloquear al servicio de origen).
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
