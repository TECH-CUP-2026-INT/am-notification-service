package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.exception;

import co.edu.escuelaing.techcup.notifications.domain.exception.NotificationAccessDeniedException;
import co.edu.escuelaing.techcup.notifications.domain.exception.NotificationNotFoundException;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final Map<HttpStatus, String> ESTADO_EN_ESPANOL = Map.of(
            HttpStatus.BAD_REQUEST, "Solicitud inválida",
            HttpStatus.UNAUTHORIZED, "No autenticado",
            HttpStatus.FORBIDDEN, "Acceso denegado",
            HttpStatus.NOT_FOUND, "No encontrado",
            HttpStatus.INTERNAL_SERVER_ERROR, "Error interno");

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotificationNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(NotificationAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleNotificationAccessDenied(
            NotificationAccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Cuerpo de la solicitud inválido o incompleto", request);
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthenticated(InsufficientAuthenticationException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "No tiene permisos suficientes para esta acción", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado procesando {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        String errorEnEspanol = ESTADO_EN_ESPANOL.getOrDefault(status, status.getReasonPhrase());
        ErrorResponse body = new ErrorResponse(Instant.now(), status.value(), errorEnEspanol, message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
