package co.edu.escuelaing.techcup.notifications.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "techcup.messaging")
public record MessagingProperties(String exchange, String dlxExchange, String sharedExchange) {
}
