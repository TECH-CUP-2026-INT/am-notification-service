package co.edu.escuelaing.techcup.notifications.email;

/** Envuelve cualquier falla al construir o enviar un correo (JavaMailSender lanza checked exceptions). */
public class EmailDeliveryException extends RuntimeException {

    public EmailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
