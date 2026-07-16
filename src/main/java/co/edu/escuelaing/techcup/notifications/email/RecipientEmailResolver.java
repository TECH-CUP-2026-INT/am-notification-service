package co.edu.escuelaing.techcup.notifications.email;

import java.util.Optional;
import java.util.UUID;

/**
 * Resuelve la dirección de correo de un destinatario a partir de su {@code recipientId}.
 * Ningún evento que llega a este servicio trae un email — solo el UUID de la cuenta —
 * así que esta resolución depende de un servicio externo dueño de esa información.
 */
public interface RecipientEmailResolver {

    Optional<String> resolveEmail(UUID recipientId);
}
