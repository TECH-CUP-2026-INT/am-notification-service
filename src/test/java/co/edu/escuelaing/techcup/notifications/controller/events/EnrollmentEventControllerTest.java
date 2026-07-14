package co.edu.escuelaing.techcup.notifications.controller.events;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.escuelaing.techcup.notifications.config.InternalApiKeyProperties;
import co.edu.escuelaing.techcup.notifications.config.SecurityConfig;
import co.edu.escuelaing.techcup.notifications.dto.event.EnrollmentProofReceivedEvent;
import co.edu.escuelaing.techcup.notifications.dto.event.EnrollmentStatus;
import co.edu.escuelaing.techcup.notifications.dto.event.EnrollmentStatusChangedEvent;
import co.edu.escuelaing.techcup.notifications.exception.GlobalExceptionHandler;
import co.edu.escuelaing.techcup.notifications.listener.EnrollmentEventListener;
import co.edu.escuelaing.techcup.notifications.security.CurrentUserProvider;
import co.edu.escuelaing.techcup.notifications.security.InternalApiKeyFilter;
import co.edu.escuelaing.techcup.notifications.security.JwtClaimsFilter;
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
        EnrollmentStatusChangedEvent event = new EnrollmentStatusChangedEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), EnrollmentStatus.APROBADA, null, Instant.now());

        mockMvc.perform(post("/api/notificaciones/inscripciones/estado")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isAccepted());

        verify(enrollmentEventListener).onStatusChanged(any(EnrollmentStatusChangedEvent.class));
    }

    @Test
    void receiveProofReceived_withValidInternalApiKey_delegatesToListenerAndReturns202() throws Exception {
        EnrollmentProofReceivedEvent event = new EnrollmentProofReceivedEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "https://example.com/comprobante.pdf", Instant.now());

        mockMvc.perform(post("/api/notificaciones/inscripciones/comprobante")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isAccepted());

        verify(enrollmentEventListener).onProofReceived(any(EnrollmentProofReceivedEvent.class));
    }

    @Test
    void receiveStatusChanged_withoutAnyCredentials_isRejected() throws Exception {
        EnrollmentStatusChangedEvent event = new EnrollmentStatusChangedEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), EnrollmentStatus.APROBADA, null, Instant.now());

        mockMvc.perform(post("/api/notificaciones/inscripciones/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isForbidden());

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
