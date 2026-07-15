package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.swagger;

import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.PlayerSanctionedRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Webhooks de eventos", description = "Endpoints servicio-a-servicio protegidos con API key interna")
public interface SanctionEventApi {

    @Operation(
            summary = "Registrar una sanción por tarjetas",
            description = "CONTRATO CONFIRMADO con el Servicio de Partidos: consumido por "
                    + "RestSanctionNotifier cuando un jugador acumula sanción por tarjetas amarillas/rojas.")
    @RequestBody(required = true, content = @Content(examples = @ExampleObject(value = """
            {
              "matchId": "11111111-1111-1111-1111-111111111111",
              "teamId": "22222222-2222-2222-2222-222222222222",
              "playerId": "33333333-3333-3333-3333-333333333333",
              "triggeringCardType": "YELLOW",
              "yellowCardsInMatch": 2,
              "occurredAt": "2026-07-11T20:00:00Z"
            }""")))
    @ApiResponse(responseCode = "202", description = "Notificación aceptada para procesamiento")
    @ApiResponse(responseCode = "400", description = "Payload inválido (campos faltantes o mal formados)")
    @ApiResponse(responseCode = "401", description = "Falta o es inválida la API key interna")
    ResponseEntity<Void> receive(PlayerSanctionedRequest request);
}
