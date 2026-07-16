package co.edu.escuelaing.techcup.notifications.entity.enums;

/**
 * Tipo de evento de la notificación. Va explícito en el payload de la API (no solo un
 * color o ícono) para que el frontend pueda ofrecer una etiqueta/aria-label accesible
 * por tipo, y para que el historial sea filtrable por categoría.
 */
public enum NotificationType {
    SANCION_TARJETAS,
    SANCION_CONDUCTA,
    NUEVO_MENSAJE_CHAT,
    SOLICITUD_VINCULACION_EQUIPO,
    VINCULACION_ACEPTADA,
    VINCULACION_RECHAZADA,
    INVITACION_EQUIPO,
    INSCRIPCION_APROBADA,
    INSCRIPCION_RECHAZADA,
    INSCRIPCION_CANCELADA,
    INSCRIPCION_COMPROBANTE_RECIBIDO,
    PARTIDO_PROGRAMADO,
    PARTIDO_REPROGRAMADO,
    PARTIDO_CANCELADO,
    CAPITANIA_CEDIDA,
    CAPITANIA_SOLICITADA,
    RESULTADO_PARTIDO
}
