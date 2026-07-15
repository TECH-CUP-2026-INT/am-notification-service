package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.controller.events;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.escuelaing.techcup.notifications.domain.model.event.MatchScheduleAction;
import co.edu.escuelaing.techcup.notifications.domain.model.event.MatchScheduleEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.MatchScheduleEventListener;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.InternalApiKeyProperties;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.SecurityConfig;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.CurrentUserProvider;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.InternalApiKeyFilter;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.JwtClaimsFilter;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.MatchScheduleRequest;
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

@WebMvcTest(MatchScheduleEventController.class)
@Import({SecurityConfig.class, JwtClaimsFilter.class, InternalApiKeyFilter.class, GlobalExceptionHandler.class,
        CurrentUserProvider.class})
@TestPropertySource(properties = "techcup.security.internal.api-key=test-internal-key")
@org.springframework.boot.context.properties.EnableConfigurationProperties(InternalApiKeyProperties.class)
class MatchScheduleEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MatchScheduleEventListener matchScheduleEventListener;

    @Test
    void receive_withValidInternalApiKey_delegatesToListenerAndReturns202() throws Exception {
        MatchScheduleRequest request = new MatchScheduleRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                MatchScheduleAction.PROGRAMADO, Instant.now(), null, Instant.now());

        mockMvc.perform(post("/api/notificaciones/partidos")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(matchScheduleEventListener).onScheduleChanged(any(MatchScheduleEvent.class));
    }

    @Test
    void receive_withoutAnyCredentials_isRejected() throws Exception {
        MatchScheduleRequest request = new MatchScheduleRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                MatchScheduleAction.PROGRAMADO, Instant.now(), null, Instant.now());

        mockMvc.perform(post("/api/notificaciones/partidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(matchScheduleEventListener);
    }

    @Test
    void receive_invalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/notificaciones/partidos")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(matchScheduleEventListener);
    }

    @Test
    void receive_cancelledWithoutScheduledAt_isAccepted() throws Exception {
        MatchScheduleRequest request = new MatchScheduleRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                MatchScheduleAction.CANCELADO, null, null, Instant.now());

        mockMvc.perform(post("/api/notificaciones/partidos")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    @Test
    void receive_programadoWithoutScheduledAt_returnsBadRequest() throws Exception {
        MatchScheduleRequest request = new MatchScheduleRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                MatchScheduleAction.PROGRAMADO, null, null, Instant.now());

        mockMvc.perform(post("/api/notificaciones/partidos")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(matchScheduleEventListener);
    }

    @Test
    void receive_previousScheduledAtWithoutReprogramado_returnsBadRequest() throws Exception {
        MatchScheduleRequest request = new MatchScheduleRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                MatchScheduleAction.PROGRAMADO, Instant.now(), Instant.now(), Instant.now());

        mockMvc.perform(post("/api/notificaciones/partidos")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(matchScheduleEventListener);
    }
}
