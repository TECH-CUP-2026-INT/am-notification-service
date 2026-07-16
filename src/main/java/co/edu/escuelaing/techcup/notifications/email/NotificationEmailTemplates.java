package co.edu.escuelaing.techcup.notifications.email;

import co.edu.escuelaing.techcup.notifications.entity.enums.NotificationType;
import java.time.Year;

/**
 * Asunto y cuerpo HTML del correo para cada {@link NotificationType}. El switch es
 * exhaustivo a propósito (sin {@code default}): agregar un NotificationType nuevo sin
 * agregarle asunto aquí rompe la compilación, en vez de mandar un correo con un asunto
 * genérico sin que nadie lo note.
 */
public final class NotificationEmailTemplates {

    private NotificationEmailTemplates() {
    }

    public static String subjectFor(NotificationType type) {
        return switch (type) {
            case SANCION_TARJETAS -> "Sanción por tarjetas";
            case NUEVO_MENSAJE_CHAT -> "Nuevo mensaje";
            case SOLICITUD_VINCULACION_EQUIPO -> "Solicitud de vinculación a equipo";
            case VINCULACION_ACEPTADA -> "Tu solicitud de vinculación fue aceptada";
            case VINCULACION_RECHAZADA -> "Tu solicitud de vinculación fue rechazada";
            case INVITACION_EQUIPO -> "Invitación a equipo";
            case INSCRIPCION_APROBADA -> "Inscripción aprobada";
            case INSCRIPCION_RECHAZADA -> "Inscripción rechazada";
            case INSCRIPCION_CANCELADA -> "Inscripción cancelada";
            case INSCRIPCION_COMPROBANTE_RECIBIDO -> "Comprobante de inscripción recibido";
            case PARTIDO_PROGRAMADO -> "Partido programado";
            case PARTIDO_REPROGRAMADO -> "Partido reprogramado";
            case PARTIDO_CANCELADO -> "Partido cancelado";
            case CAPITANIA_CEDIDA -> "Te delegaron la capitanía del equipo";
            case CAPITANIA_SOLICITADA -> "Solicitud de capitanía";
        };
    }

    public static String bodyFor(String subject, String message) {
        return """
                <!DOCTYPE html>
                <html>
                <head><style>
                    body { font-family: Arial, sans-serif; background: #f4f4f4; padding: 20px; }
                    .container { max-width: 500px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; }
                    .message { font-size: 16px; color: #333; padding: 16px; background: #f0f0f0; border-radius: 8px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; font-size: 12px; color: #999; }
                </style></head>
                <body>
                    <div class="container">
                        <h2>🏆 TechCup - %s</h2>
                        <div class="message">%s</div>
                        <div class="footer">© %d TechCup</div>
                    </div>
                </body>
                </html>
                """.formatted(subject, message, Year.now().getValue());
    }
}
