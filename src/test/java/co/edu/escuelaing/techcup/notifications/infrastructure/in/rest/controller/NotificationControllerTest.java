package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.escuelaing.techcup.notifications.application.mapper.NotificationMapper;
import co.edu.escuelaing.techcup.notifications.domain.model.Notification;
import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.NotificationUseCase;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.SecurityConfig;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.CurrentUserProvider;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.InternalApiKeyFilter;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.JwtClaimsFilter;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.exception.GlobalExceptionHandler;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.InternalApiKeyProperties;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(NotificationController.class)
@Import({SecurityConfig.class, JwtClaimsFilter.class, InternalApiKeyFilter.class, GlobalExceptionHandler.class,
        CurrentUserProvider.class, NotificationMapper.class})
@TestPropertySource(properties = "techcup.security.internal.api-key=test-internal-key")
@org.springframework.boot.context.properties.EnableConfigurationProperties(InternalApiKeyProperties.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationUseCase notificationUseCase;

    @Test
    void list_withValidJwt_returnsMappedNotifications() throws Exception {
        UUID userId = UUID.randomUUID();
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setType(NotificationType.NUEVO_MENSAJE_CHAT);
        notification.setMessage("hola");
        notification.setRecipientId(userId);
        notification.setCreatedAt(Instant.now());
        when(notificationUseCase.listForUser(eq(userId), eq(null))).thenReturn(List.of(notification));

        mockMvc.perform(get("/api/notificaciones").header("Authorization", "Bearer " + jwtFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].message").value("hola"));
    }

    @Test
    void list_withoutAnyCredentials_isRejected() throws Exception {
        // Sin credenciales: la autenticación anónima de Spring Security entra en juego y
        // la regla de autorización rechaza la solicitud (403) antes de llegar a
        // CurrentUserProvider/GlobalExceptionHandler.
        mockMvc.perform(get("/api/notificaciones"))
                .andExpect(status().isForbidden());
    }

    @Test
    void list_withOnlyInternalApiKey_isRejectedBecauseNotAnAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/notificaciones").header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unreadCount_withValidJwt_returnsCount() throws Exception {
        UUID userId = UUID.randomUUID();
        when(notificationUseCase.countUnread(userId)).thenReturn(5L);

        mockMvc.perform(get("/api/notificaciones/no-leidas/conteo").header("Authorization", "Bearer " + jwtFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    void markAsRead_withValidJwt_returnsMappedNotification() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setType(NotificationType.NUEVO_MENSAJE_CHAT);
        notification.setMessage("hola");
        notification.setRecipientId(userId);
        notification.setRead(true);
        notification.setCreatedAt(Instant.now());
        when(notificationUseCase.markAsRead(notificationId, userId)).thenReturn(notification);

        mockMvc.perform(patch("/api/notificaciones/{id}/leer", notificationId)
                        .header("Authorization", "Bearer " + jwtFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void markAllAsRead_withValidJwt_returnsNoContent() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(patch("/api/notificaciones/leer-todas").header("Authorization", "Bearer " + jwtFor(userId)))
                .andExpect(status().isNoContent());

        verify(notificationUseCase).markAllAsRead(userId);
    }

    private static String jwtFor(UUID userId) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("{\"sub\":\"" + userId + "\"}").getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".sig";
    }
}
