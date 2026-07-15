package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.controller.events;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.escuelaing.techcup.notifications.domain.model.event.CaptaincyTransferEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.CaptaincyTransferInitiator;
import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamInvitationEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamLinkRequestedEvent;
import co.edu.escuelaing.techcup.notifications.domain.model.event.TeamLinkRespondedEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CaptaincyTransferEventListener;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.TeamLinkEventListener;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.InternalApiKeyProperties;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.SecurityConfig;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.CurrentUserProvider;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.InternalApiKeyFilter;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.JwtClaimsFilter;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.CaptaincyTransferRequest;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.TeamInvitationRequest;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.TeamLinkRequestedRequest;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.TeamLinkRespondedRequest;
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

@WebMvcTest(TeamEventController.class)
@Import({SecurityConfig.class, JwtClaimsFilter.class, InternalApiKeyFilter.class, GlobalExceptionHandler.class,
        CurrentUserProvider.class})
@TestPropertySource(properties = "techcup.security.internal.api-key=test-internal-key")
@org.springframework.boot.context.properties.EnableConfigurationProperties(InternalApiKeyProperties.class)
class TeamEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TeamLinkEventListener teamLinkEventListener;

    @MockitoBean
    private CaptaincyTransferEventListener captaincyTransferEventListener;

    @Test
    void receiveLinkRequest_withValidInternalApiKey_delegatesToListenerAndReturns202() throws Exception {
        TeamLinkRequestedRequest request = new TeamLinkRequestedRequest(
                UUID.randomUUID(), "Los Tigres", UUID.randomUUID(), "Ana", UUID.randomUUID(), UUID.randomUUID(),
                Instant.now());

        mockMvc.perform(post("/api/notificaciones/equipos/solicitudes")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(teamLinkEventListener).onLinkRequested(any(TeamLinkRequestedEvent.class));
    }

    @Test
    void receiveLinkResponse_withValidInternalApiKey_delegatesToListenerAndReturns202() throws Exception {
        TeamLinkRespondedRequest request = new TeamLinkRespondedRequest(
                UUID.randomUUID(), "Los Tigres", UUID.randomUUID(), UUID.randomUUID(), true, Instant.now());

        mockMvc.perform(post("/api/notificaciones/equipos/respuestas")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(teamLinkEventListener).onLinkResponded(any(TeamLinkRespondedEvent.class));
    }

    @Test
    void receiveInvitation_withValidInternalApiKey_delegatesToListenerAndReturns202() throws Exception {
        TeamInvitationRequest request = new TeamInvitationRequest(
                UUID.randomUUID(), "Los Tigres", UUID.randomUUID(), UUID.randomUUID(), "Ana", Instant.now());

        mockMvc.perform(post("/api/notificaciones/equipos/invitaciones")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(teamLinkEventListener).onTeamInvited(any(TeamInvitationEvent.class));
    }

    @Test
    void receiveCaptaincyTransfer_withValidInternalApiKey_delegatesToListenerAndReturns202() throws Exception {
        CaptaincyTransferRequest request = new CaptaincyTransferRequest(
                UUID.randomUUID(), "Los Tigres", UUID.randomUUID(), UUID.randomUUID(),
                CaptaincyTransferInitiator.DELEGATION, Instant.now());

        mockMvc.perform(post("/api/notificaciones/equipos/capitania")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(captaincyTransferEventListener).onCaptaincyTransferred(any(CaptaincyTransferEvent.class));
    }

    @Test
    void receiveCaptaincyTransfer_withOnlyUserJwt_isRejected() throws Exception {
        // SecurityConfig exige ROLE_SERVICIO_INTERNO en los webhooks: un JWT de usuario
        // final (autenticado solo como ROLE_USER) no debe poder falsificar un evento de
        // otro servicio.
        CaptaincyTransferRequest request = new CaptaincyTransferRequest(
                UUID.randomUUID(), "Los Tigres", UUID.randomUUID(), UUID.randomUUID(),
                CaptaincyTransferInitiator.APPLICATION, Instant.now());

        mockMvc.perform(post("/api/notificaciones/equipos/capitania")
                        .header("Authorization", "Bearer " + jwtFor(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(captaincyTransferEventListener);
    }

    static String jwtFor(UUID userId) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("{\"sub\":\"" + userId + "\"}").getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".sig";
    }

    @Test
    void receiveLinkRequest_withoutAnyCredentials_isRejected() throws Exception {
        TeamLinkRequestedRequest request = new TeamLinkRequestedRequest(
                UUID.randomUUID(), "Los Tigres", UUID.randomUUID(), "Ana", UUID.randomUUID(), UUID.randomUUID(),
                Instant.now());

        mockMvc.perform(post("/api/notificaciones/equipos/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(teamLinkEventListener);
    }

    @Test
    void receiveLinkRequest_invalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/notificaciones/equipos/solicitudes")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(teamLinkEventListener);
    }

    @Test
    void receiveLinkResponse_invalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/notificaciones/equipos/respuestas")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(teamLinkEventListener);
    }

    @Test
    void receiveInvitation_invalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/notificaciones/equipos/invitaciones")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(teamLinkEventListener);
    }

    @Test
    void receiveCaptaincyTransfer_invalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/notificaciones/equipos/capitania")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(captaincyTransferEventListener);
    }
}
