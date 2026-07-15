package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.controller.events;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.escuelaing.techcup.notifications.domain.model.event.ChatMessageEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.ChatEventListener;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.InternalApiKeyProperties;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.SecurityConfig;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.CurrentUserProvider;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.InternalApiKeyFilter;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.JwtClaimsFilter;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.request.ChatMessageRequest;
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

@WebMvcTest(ChatEventController.class)
@Import({SecurityConfig.class, JwtClaimsFilter.class, InternalApiKeyFilter.class, GlobalExceptionHandler.class,
        CurrentUserProvider.class})
@TestPropertySource(properties = "techcup.security.internal.api-key=test-internal-key")
@org.springframework.boot.context.properties.EnableConfigurationProperties(InternalApiKeyProperties.class)
class ChatEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatEventListener chatEventListener;

    @Test
    void receive_withValidInternalApiKey_delegatesToListenerAndReturns202() throws Exception {
        ChatMessageRequest request = new ChatMessageRequest(
                UUID.randomUUID(), UUID.randomUUID(), "Ana", UUID.randomUUID(), "hola equipo", Instant.now());

        mockMvc.perform(post("/api/notificaciones/mensajes")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(chatEventListener).onNewChatMessage(any(ChatMessageEvent.class));
    }

    @Test
    void receive_withoutAnyCredentials_isRejected() throws Exception {
        ChatMessageRequest request = new ChatMessageRequest(
                UUID.randomUUID(), UUID.randomUUID(), "Ana", UUID.randomUUID(), "hola equipo", Instant.now());

        mockMvc.perform(post("/api/notificaciones/mensajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(chatEventListener);
    }

    @Test
    void receive_invalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/notificaciones/mensajes")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(chatEventListener);
    }

    @Test
    void receive_messagePreviewTooLong_returnsBadRequest() throws Exception {
        ChatMessageRequest request = new ChatMessageRequest(
                UUID.randomUUID(), UUID.randomUUID(), "Ana", UUID.randomUUID(), "x".repeat(501), Instant.now());

        mockMvc.perform(post("/api/notificaciones/mensajes")
                        .header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(chatEventListener);
    }
}
