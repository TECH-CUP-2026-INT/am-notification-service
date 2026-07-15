package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.controller.events;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.escuelaing.techcup.notifications.domain.model.event.CardType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.PlayerSanctionedEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.SanctionEventListener;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.InternalApiKeyProperties;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.SecurityConfig;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.CurrentUserProvider;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.InternalApiKeyFilter;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.JwtClaimsFilter;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.PlayerSanctionedRequest;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SanctionEventController.class)
@Import({SecurityConfig.class, JwtClaimsFilter.class, InternalApiKeyFilter.class, GlobalExceptionHandler.class,
        CurrentUserProvider.class})
@TestPropertySource(properties = "techcup.security.internal.api-key=test-internal-key")
@org.springframework.boot.context.properties.EnableConfigurationProperties(InternalApiKeyProperties.class)
class SanctionEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SanctionEventListener sanctionEventListener;

    @Test
    void receive_withValidInternalApiKey_delegatesToListenerAndReturns202() throws Exception {
        PlayerSanctionedRequest request = new PlayerSanctionedRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), CardType.RED, 2, Instant.now());

        mockMvc.perform(post("/api/notificaciones/sanciones")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(sanctionEventListener).onPlayerSanctioned(any(PlayerSanctionedEvent.class));
    }

    @Test
    void receive_withoutAnyCredentials_isRejected() throws Exception {
        PlayerSanctionedRequest request = new PlayerSanctionedRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), CardType.RED, 2, Instant.now());

        mockMvc.perform(post("/api/notificaciones/sanciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(sanctionEventListener);
    }

    @Test
    void receive_withWrongInternalApiKey_isRejected() throws Exception {
        PlayerSanctionedRequest request = new PlayerSanctionedRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), CardType.RED, 2, Instant.now());

        mockMvc.perform(post("/api/notificaciones/sanciones")
                        .header(InternalApiKeyFilter.HEADER_NAME, "wrong-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(sanctionEventListener);
    }

    @Test
    void receive_invalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/notificaciones/sanciones")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(sanctionEventListener);
    }

    @Test
    void receive_withOnlyUserJwt_isRejected() throws Exception {
        // SecurityConfig exige ROLE_SERVICIO_INTERNO en los webhooks: un JWT de usuario
        // final (autenticado solo como ROLE_USER) no debe poder falsificar un evento de
        // otro servicio.
        PlayerSanctionedRequest request = new PlayerSanctionedRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), CardType.RED, 2, Instant.now());

        mockMvc.perform(post("/api/notificaciones/sanciones")
                        .header("Authorization", "Bearer " + jwtFor(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(sanctionEventListener);
    }

    static String jwtFor(UUID userId) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("{\"sub\":\"" + userId + "\"}").getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".sig";
    }
}
