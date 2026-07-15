package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.swagger;

import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.ChatMessageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Webhooks de eventos", description = "Endpoints servicio-a-servicio protegidos con API key interna")
public interface ChatEventApi {

    @Operation(
            summary = "Registrar un nuevo mensaje de chat",
            description = "CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de "
                    + "Comunicaciones. Consumido por ese servicio para notificar al destinatario de un chat.")
    @RequestBody(required = true, content = @Content(examples = @ExampleObject(value = """
            {
              "chatId": "44444444-4444-4444-4444-444444444444",
              "senderId": "55555555-5555-5555-5555-555555555555",
              "senderName": "Julián Tinjacá",
              "recipientId": "22222222-2222-2222-2222-222222222222",
              "messagePreview": "Hola, ¿confirmamos la alineación?",
              "sentAt": "2026-07-11T20:00:00Z"
            }""")))
    @ApiResponse(responseCode = "202", description = "Notificación aceptada para procesamiento")
    @ApiResponse(responseCode = "400", description = "Payload inválido (campos faltantes o mal formados)")
    @ApiResponse(responseCode = "401", description = "Falta o es inválida la API key interna")
    ResponseEntity<Void> receive(ChatMessageRequest request);
}
