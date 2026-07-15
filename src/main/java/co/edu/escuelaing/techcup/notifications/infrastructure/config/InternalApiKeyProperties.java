package co.edu.escuelaing.techcup.notifications.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "techcup.security.internal")
public record InternalApiKeyProperties(String apiKey) {
}
