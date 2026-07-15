package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.swagger;

import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.MatchScheduleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Webhooks de eventos", description = "Endpoints servicio-a-servicio protegidos con API key interna")
public interface MatchScheduleEventApi {

    @Operation(
            summary = "Registrar cambio de agendamiento de un partido",
            description = "CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de "
                    + "Agendamiento. `scheduledAt` es obligatorio salvo cuando `action` es CANCELADO; "
                    + "`previousScheduledAt` solo aplica cuando `action` es REPROGRAMADO.")
    @RequestBody(required = true, content = @Content(examples = @ExampleObject(value = """
            {
              "matchId": "77777777-7777-7777-7777-777777777777",
              "teamHomeId": "22222222-2222-2222-2222-222222222222",
              "teamAwayId": "88888888-8888-8888-8888-888888888888",
              "recipientId": "33333333-3333-3333-3333-333333333333",
              "action": "PROGRAMADO",
              "scheduledAt": "2026-07-20T15:00:00Z",
              "previousScheduledAt": null,
              "occurredAt": "2026-07-11T20:00:00Z"
            }""")))
    @ApiResponse(responseCode = "202", description = "Notificación aceptada para procesamiento")
    @ApiResponse(responseCode = "400", description = "Payload inválido (campos faltantes, mal formados, o "
            + "que violan la regla condicional de scheduledAt/previousScheduledAt)")
    @ApiResponse(responseCode = "401", description = "Falta o es inválida la API key interna")
    ResponseEntity<Void> receive(MatchScheduleRequest request);
}
