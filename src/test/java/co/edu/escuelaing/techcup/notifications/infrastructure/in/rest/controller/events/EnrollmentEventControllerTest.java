package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.controller.events;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentProofReceivedEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentStatus;
import co.edu.escuelaing.techcup.notifications.domain.model.event.EnrollmentStatusChangedEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.EnrollmentEventListener;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.InternalApiKeyProperties;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.SecurityConfig;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.CurrentUserProvider;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.InternalApiKeyFilter;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.JwtClaimsFilter;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.EnrollmentProofReceivedRequest;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.EnrollmentStatusChangedRequest;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EnrollmentEventController.class)
@Import({SecurityConfig.class, JwtClaimsFilter.class, InternalApiKeyFilter.class, GlobalExceptionHandler.class,
        CurrentUserProvider.class})
@TestPropertySource(properties = "techcup.security.internal.api-key=test-internal-key")
@org.springframework.boot.context.properties.EnableConfigurationProperties(InternalApiKeyProperties.class)
class EnrollmentEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EnrollmentEventListener enrollmentEventListener;

    @Test
    void receiveStatusChanged_withValidInternalApiKey_delegatesToListenerAndReturns202() throws Exception {
        EnrollmentStatusChangedRequest request = new EnrollmentStatusChangedRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), EnrollmentStatus.APROBADA, null, Instant.now());

        mockMvc.perform(post("/api/notificaciones/inscripciones/estado")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(enrollmentEventListener).onStatusChanged(any(EnrollmentStatusChangedEvent.class));
    }

    @Test
    void receiveProofReceived_withValidInternalApiKey_delegatesToListenerAndReturns202() throws Exception {
        EnrollmentProofReceivedRequest request = new EnrollmentProofReceivedRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "https://example.com/comprobante.pdf", Instant.now());

        mockMvc.perform(post("/api/notificaciones/inscripciones/comprobante")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(enrollmentEventListener).onProofReceived(any(EnrollmentProofReceivedEvent.class));
    }

    @Test
    void receiveStatusChanged_withoutAnyCredentials_isRejected() throws Exception {
        EnrollmentStatusChangedRequest request = new EnrollmentStatusChangedRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), EnrollmentStatus.APROBADA, null, Instant.now());

        mockMvc.perform(post("/api/notificaciones/inscripciones/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(enrollmentEventListener);
    }

    @Test
    void receiveStatusChanged_invalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/notificaciones/inscripciones/estado")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(enrollmentEventListener);
    }

    @Test
    void receiveProofReceived_invalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/notificaciones/inscripciones/comprobante")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(enrollmentEventListener);
    }
}
