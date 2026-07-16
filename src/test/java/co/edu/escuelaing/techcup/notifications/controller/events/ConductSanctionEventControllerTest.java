package co.edu.escuelaing.techcup.notifications.controller.events;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.escuelaing.techcup.notifications.config.InternalApiKeyProperties;
import co.edu.escuelaing.techcup.notifications.config.SecurityConfig;
import co.edu.escuelaing.techcup.notifications.dto.event.PlayerConductSanctionedEvent;
import co.edu.escuelaing.techcup.notifications.exception.GlobalExceptionHandler;
import co.edu.escuelaing.techcup.notifications.listener.ConductSanctionEventListener;
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

@WebMvcTest(ConductSanctionEventController.class)
@Import({SecurityConfig.class, JwtClaimsFilter.class, InternalApiKeyFilter.class, GlobalExceptionHandler.class,
        CurrentUserProvider.class})
@TestPropertySource(properties = "techcup.security.internal.api-key=test-internal-key")
@org.springframework.boot.context.properties.EnableConfigurationProperties(InternalApiKeyProperties.class)
class ConductSanctionEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConductSanctionEventListener conductSanctionEventListener;

    @Test
    void receive_withValidInternalApiKey_delegatesToListenerAndReturns202() throws Exception {
        PlayerConductSanctionedEvent event = new PlayerConductSanctionedEvent(
                UUID.randomUUID().toString(), 2, "Agresión a un árbitro", Instant.now());

        mockMvc.perform(post("/api/notificaciones/sanciones-conducta")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isAccepted());

        verify(conductSanctionEventListener).onPlayerConductSanctioned(any(PlayerConductSanctionedEvent.class));
    }

    @Test
    void receive_withoutAnyCredentials_isRejected() throws Exception {
        PlayerConductSanctionedEvent event = new PlayerConductSanctionedEvent(
                UUID.randomUUID().toString(), 2, "Agresión a un árbitro", Instant.now());

        mockMvc.perform(post("/api/notificaciones/sanciones-conducta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(conductSanctionEventListener);
    }

    @Test
    void receive_withWrongInternalApiKey_isRejected() throws Exception {
        PlayerConductSanctionedEvent event = new PlayerConductSanctionedEvent(
                UUID.randomUUID().toString(), 2, "Agresión a un árbitro", Instant.now());

        mockMvc.perform(post("/api/notificaciones/sanciones-conducta")
                        .header(InternalApiKeyFilter.HEADER_NAME, "wrong-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(conductSanctionEventListener);
    }

    @Test
    void receive_invalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/notificaciones/sanciones-conducta")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(conductSanctionEventListener);
    }

    @Test
    void receive_matchesSuspendedZero_returnsBadRequest() throws Exception {
        String body = """
                {"playerId":"%s","matchesSuspended":0,"reason":"Motivo","occurredAt":"%s"}
                """.formatted(UUID.randomUUID(), Instant.now());

        mockMvc.perform(post("/api/notificaciones/sanciones-conducta")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(conductSanctionEventListener);
    }
}
