package co.edu.escuelaing.techcup.notifications.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import co.edu.escuelaing.techcup.notifications.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/notificaciones");
    }

    @Test
    void handleNotFound_buildsNotFoundResponse() {
        UUID id = UUID.randomUUID();
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(new NotificationNotFoundException(id), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertBody(response.getBody(), HttpStatus.NOT_FOUND, "No existe la notificación " + id);
    }

    @Test
    void handleNotificationAccessDenied_buildsForbiddenResponse() {
        UUID id = UUID.randomUUID();
        ResponseEntity<ErrorResponse> response =
                handler.handleNotificationAccessDenied(new NotificationAccessDeniedException(id), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertBody(response.getBody(), HttpStatus.FORBIDDEN, "No tiene permisos sobre la notificación " + id);
    }

    @Test
    void handleValidation_buildsBadRequestWithFieldErrors() {
        MethodArgumentNotValidException ex = mockValidationException();

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("campo: debe no ser nulo");
        assertThat(response.getBody().path()).isEqualTo("/api/notificaciones");
    }

    @Test
    void handleUnreadableBody_buildsBadRequestWithFixedMessage() {
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("bad json", (org.springframework.http.HttpInputMessage) null);

        ResponseEntity<ErrorResponse> response = handler.handleUnreadableBody(ex, request);

        assertBody(response.getBody(), HttpStatus.BAD_REQUEST, "Cuerpo de la solicitud inválido o incompleto");
    }

    @Test
    void handleUnauthenticated_buildsUnauthorizedResponse() {
        InsufficientAuthenticationException ex = new InsufficientAuthenticationException("no auth");

        ResponseEntity<ErrorResponse> response = handler.handleUnauthenticated(ex, request);

        assertBody(response.getBody(), HttpStatus.UNAUTHORIZED, "no auth");
    }

    @Test
    void handleForbidden_buildsForbiddenWithFixedMessage() {
        AccessDeniedException ex = new AccessDeniedException("denied");

        ResponseEntity<ErrorResponse> response = handler.handleForbidden(ex, request);

        assertBody(response.getBody(), HttpStatus.FORBIDDEN, "No tiene permisos suficientes para esta acción");
    }

    @Test
    void handleUnexpected_buildsInternalServerErrorWithFixedMessage() {
        when(request.getMethod()).thenReturn("GET");
        Exception ex = new RuntimeException("boom");

        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(ex, request);

        assertBody(response.getBody(), HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado");
    }

    private void assertBody(ErrorResponse body, HttpStatus status, String message) {
        assertThat(body.status()).isEqualTo(status.value());
        assertThat(body.error()).isEqualTo(status.getReasonPhrase());
        assertThat(body.message()).isEqualTo(message);
        assertThat(body.path()).isEqualTo("/api/notificaciones");
        assertThat(body.timestamp()).isNotNull();
    }

    private MethodArgumentNotValidException mockValidationException() {
        BindingResult bindingResult = org.mockito.Mockito.mock(BindingResult.class);
        FieldError fieldError = new FieldError("target", "campo", "debe no ser nulo");
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));
        return new MethodArgumentNotValidException((org.springframework.core.MethodParameter) null, bindingResult);
    }
}
