package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.swagger;

import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.EnrollmentProofReceivedRequest;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.EnrollmentStatusChangedRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Webhooks de eventos", description = "Endpoints servicio-a-servicio protegidos con API key interna")
public interface EnrollmentEventApi {

    @Operation(
            summary = "Registrar cambio de estado de inscripción",
            description = "CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de "
                    + "Inscripción. `reason` es opcional (por ejemplo, motivo de rechazo).")
    @RequestBody(required = true, content = @Content(examples = @ExampleObject(value = """
            {
              "enrollmentId": "66666666-6666-6666-6666-666666666666",
              "teamId": "22222222-2222-2222-2222-222222222222",
              "recipientId": "33333333-3333-3333-3333-333333333333",
              "newStatus": "APROBADA",
              "reason": null,
              "occurredAt": "2026-07-11T20:00:00Z"
            }""")))
    @ApiResponse(responseCode = "202", description = "Notificación aceptada para procesamiento")
    @ApiResponse(responseCode = "400", description = "Payload inválido (campos faltantes o mal formados)")
    @ApiResponse(responseCode = "401", description = "Falta o es inválida la API key interna")
    ResponseEntity<Void> receiveStatusChanged(EnrollmentStatusChangedRequest request);

    @Operation(
            summary = "Registrar comprobante de inscripción recibido",
            description = "CONTRATO PROPUESTO — pendiente de confirmar con el equipo del Servicio de "
                    + "Inscripción. Marca la inscripción como recibida en estado \"pendiente\" (RF8).")
    @RequestBody(required = true, content = @Content(examples = @ExampleObject(value = """
            {
              "enrollmentId": "66666666-6666-6666-6666-666666666666",
              "teamId": "22222222-2222-2222-2222-222222222222",
              "recipientId": "33333333-3333-3333-3333-333333333333",
              "proofUrl": "https://storage.techcup.com/comprobantes/66666666.pdf",
              "receivedAt": "2026-07-11T20:00:00Z"
            }""")))
    @ApiResponse(responseCode = "202", description = "Notificación aceptada para procesamiento")
    @ApiResponse(responseCode = "400", description = "Payload inválido (campos faltantes o mal formados)")
    @ApiResponse(responseCode = "401", description = "Falta o es inválida la API key interna")
    ResponseEntity<Void> receiveProofReceived(EnrollmentProofReceivedRequest request);
}
