package co.edu.escuelaing.techcup.notifications.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "techcup.email")
public record EmailProperties(String from, String testRecipient) {
}
