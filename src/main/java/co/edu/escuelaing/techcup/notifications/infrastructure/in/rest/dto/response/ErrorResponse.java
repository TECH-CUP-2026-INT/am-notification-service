package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.response;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
