package co.edu.escuelaing.techcup.notifications.email;

import co.edu.escuelaing.techcup.notifications.config.EmailProperties;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Implementación placeholder de {@link RecipientEmailResolver}, mientras no exista en el
 * org un servicio que exponga el correo real de un usuario a partir de su id (ni
 * cc-identity-service ni cc-users-players-service tienen hoy ese endpoint). Devuelve
 * siempre la misma dirección configurada en {@code techcup.email.test-recipient} — útil
 * para probar el envío real de correos en desarrollo — o nada si no está configurada.
 *
 * <p>TODO: reemplazar por un adaptador que llame a GET /usuarios/{recipientId} (o
 * equivalente) del Servicio de Usuarios una vez que exista, resolviendo el correo real
 * por destinatario en lugar de una dirección fija para todos.
 */
@Component
public class ConfiguredRecipientEmailResolver implements RecipientEmailResolver {

    private final EmailProperties properties;

    public ConfiguredRecipientEmailResolver(EmailProperties properties) {
        this.properties = properties;
    }

    @Override
    public Optional<String> resolveEmail(UUID recipientId) {
        String testRecipient = properties.testRecipient();
        return (testRecipient == null || testRecipient.isBlank())
                ? Optional.empty()
                : Optional.of(testRecipient);
    }
}
