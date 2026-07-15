package co.edu.escuelaing.techcup.notifications.infrastructure.config.security;

import java.util.UUID;

/**
 * Principal de seguridad construido a partir de los claims del JWT ya validado por el
 * API Gateway. Solo se usa en los endpoints consultados por el usuario final.
 */
public record AuthenticatedUser(UUID userId) {
}
