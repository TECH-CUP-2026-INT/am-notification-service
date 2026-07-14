package co.edu.escuelaing.techcup.notifications.dto.event;

/**
 * Quién inició el cambio de capitanía, según RF-09 de la hoja de requerimientos:
 * "Si el Capitán delega: notifica al jugador elegido. Si el jugador aplica: notifica al
 * Capitán actual."
 */
public enum CaptaincyTransferInitiator {
    /** El Capitán actual delega el rol en otro jugador — se notifica a {@code newCaptainId}. */
    DELEGATION,
    /** Un jugador aplica/solicita el rol de Capitán — se notifica a {@code currentCaptainId}. */
    APPLICATION
}
