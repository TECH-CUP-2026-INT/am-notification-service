package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.swagger;

import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.CaptaincyTransferRequest;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.TeamInvitationRequest;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.TeamLinkRequestedRequest;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.TeamLinkRespondedRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Webhooks de eventos", description = "Endpoints servicio-a-servicio protegidos con API key interna")
public interface TeamEventApi {

    @Operation(
            summary = "Registrar solicitud de vinculación a un equipo",
            description = "CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Equipos.")
    @RequestBody(required = true, content = @Content(examples = @ExampleObject(value = """
            {
              "teamId": "22222222-2222-2222-2222-222222222222",
              "teamName": "Los Tigres",
              "requesterId": "99999999-9999-9999-9999-999999999999",
              "requesterName": "Camila Rojas",
              "recipientId": "33333333-3333-3333-3333-333333333333",
              "requestId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
              "occurredAt": "2026-07-11T20:00:00Z"
            }""")))
    @ApiResponse(responseCode = "202", description = "Notificación aceptada para procesamiento")
    @ApiResponse(responseCode = "400", description = "Payload inválido (campos faltantes o mal formados)")
    @ApiResponse(responseCode = "401", description = "Falta o es inválida la API key interna")
    ResponseEntity<Void> receiveLinkRequest(TeamLinkRequestedRequest request);

    @Operation(
            summary = "Registrar respuesta a una solicitud de vinculación",
            description = "CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Equipos. "
                    + "El destinatario es quien hizo la solicitud original (no el equipo).")
    @RequestBody(required = true, content = @Content(examples = @ExampleObject(value = """
            {
              "teamId": "22222222-2222-2222-2222-222222222222",
              "teamName": "Los Tigres",
              "requestId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
              "recipientId": "99999999-9999-9999-9999-999999999999",
              "accepted": true,
              "respondedAt": "2026-07-11T20:05:00Z"
            }""")))
    @ApiResponse(responseCode = "202", description = "Notificación aceptada para procesamiento")
    @ApiResponse(responseCode = "400", description = "Payload inválido (campos faltantes o mal formados)")
    @ApiResponse(responseCode = "401", description = "Falta o es inválida la API key interna")
    ResponseEntity<Void> receiveLinkResponse(TeamLinkRespondedRequest request);

    @Operation(
            summary = "Registrar invitación a un equipo",
            description = "CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Equipos.")
    @RequestBody(required = true, content = @Content(examples = @ExampleObject(value = """
            {
              "teamId": "22222222-2222-2222-2222-222222222222",
              "teamName": "Los Tigres",
              "invitedUserId": "99999999-9999-9999-9999-999999999999",
              "invitationId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
              "invitedBy": "Camila Rojas (Capitana)",
              "occurredAt": "2026-07-11T20:00:00Z"
            }""")))
    @ApiResponse(responseCode = "202", description = "Notificación aceptada para procesamiento")
    @ApiResponse(responseCode = "400", description = "Payload inválido (campos faltantes o mal formados)")
    @ApiResponse(responseCode = "401", description = "Falta o es inválida la API key interna")
    ResponseEntity<Void> receiveInvitation(TeamInvitationRequest request);

    @Operation(
            summary = "Registrar cesión o solicitud de capitanía",
            description = "CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de Equipos. "
                    + "Cubre RF-09: si el Capitán delega, notifica al jugador elegido; si el jugador aplica, "
                    + "notifica al Capitán actual (ver initiatedBy).")
    @RequestBody(required = true, content = @Content(examples = @ExampleObject(value = """
            {
              "teamId": "22222222-2222-2222-2222-222222222222",
              "teamName": "Los Tigres",
              "currentCaptainId": "33333333-3333-3333-3333-333333333333",
              "newCaptainId": "44444444-4444-4444-4444-444444444444",
              "initiatedBy": "DELEGATION",
              "occurredAt": "2026-07-12T20:00:00Z"
            }""")))
    @ApiResponse(responseCode = "202", description = "Notificación aceptada para procesamiento")
    @ApiResponse(responseCode = "400", description = "Payload inválido (campos faltantes o mal formados)")
    @ApiResponse(responseCode = "401", description = "Falta o es inválida la API key interna")
    ResponseEntity<Void> receiveCaptaincyTransfer(CaptaincyTransferRequest request);
}
