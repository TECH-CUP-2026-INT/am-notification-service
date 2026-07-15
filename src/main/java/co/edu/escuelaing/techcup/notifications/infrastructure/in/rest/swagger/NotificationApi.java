package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.swagger;

import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.response.NotificationResponse;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.response.UnreadCountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

@Tag(name = "Notificaciones", description = "Historial de notificaciones del usuario autenticado (estilo campanita)")
public interface NotificationApi {

    @Operation(
            summary = "Listar notificaciones del usuario autenticado",
            description = "Devuelve el historial de notificaciones del usuario identificado por el JWT del "
                    + "API Gateway, ordenado por fecha de creación descendente. El filtro `leidas` es opcional.")
    @ApiResponse(responseCode = "200", description = "Historial devuelto correctamente", content = @Content(
            examples = @ExampleObject(value = """
                    [
                      {
                        "id": "1f2e3d4c-5b6a-4978-8a9b-0c1d2e3f4a5b",
                        "type": "SANCION_TARJETAS",
                        "message": "Fuiste sancionado por acumulación de tarjetas amarillas.",
                        "referenceId": "11111111-1111-1111-1111-111111111111",
                        "read": false,
                        "createdAt": "2026-07-11T20:00:01Z",
                        "readAt": null
                      }
                    ]""")))
    @ApiResponse(responseCode = "401", description = "Falta o es inválido el JWT del usuario")
    List<NotificationResponse> list(
            @Parameter(description = "Filtra por leídas (true) o no leídas (false); si se omite, trae todas")
            Boolean leidas);

    @Operation(
            summary = "Contar notificaciones no leídas",
            description = "Devuelve el conteo de notificaciones no leídas del usuario autenticado, usado para "
                    + "el ícono de campanita del frontend.")
    @ApiResponse(responseCode = "200", description = "Conteo devuelto correctamente", content = @Content(
            examples = @ExampleObject(value = "{\"count\": 3}")))
    @ApiResponse(responseCode = "401", description = "Falta o es inválido el JWT del usuario")
    UnreadCountResponse unreadCount();

    @Operation(
            summary = "Marcar una notificación como leída",
            description = "Marca como leída la notificación indicada, solo si pertenece al usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Notificación marcada como leída")
    @ApiResponse(responseCode = "401", description = "Falta o es inválido el JWT del usuario")
    @ApiResponse(responseCode = "403", description = "La notificación pertenece a otro usuario")
    @ApiResponse(responseCode = "404", description = "No existe una notificación con ese id")
    NotificationResponse markAsRead(
            @Parameter(description = "Id de la notificación a marcar como leída") UUID id);

    @Operation(
            summary = "Marcar todas las notificaciones como leídas",
            description = "Marca como leídas todas las notificaciones pendientes del usuario autenticado.")
    @ApiResponse(responseCode = "204", description = "Notificaciones marcadas como leídas")
    @ApiResponse(responseCode = "401", description = "Falta o es inválido el JWT del usuario")
    ResponseEntity<Void> markAllAsRead();
}
