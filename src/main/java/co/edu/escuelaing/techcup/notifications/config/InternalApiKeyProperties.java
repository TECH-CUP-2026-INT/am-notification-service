package co.edu.escuelaing.techcup.notifications.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "techcup.security.internal")
public record InternalApiKeyProperties(String apiKey) {
}
