package co.edu.escuelaing.techcup.notifications.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.escuelaing.techcup.notifications.config.InternalApiKeyProperties;
import co.edu.escuelaing.techcup.notifications.config.SecurityConfig;
import co.edu.escuelaing.techcup.notifications.entity.Notification;
import co.edu.escuelaing.techcup.notifications.entity.enums.NotificationType;
import co.edu.escuelaing.techcup.notifications.exception.GlobalExceptionHandler;
import co.edu.escuelaing.techcup.notifications.security.CurrentUserProvider;
import co.edu.escuelaing.techcup.notifications.security.InternalApiKeyFilter;
import co.edu.escuelaing.techcup.notifications.security.JwtClaimsFilter;
import co.edu.escuelaing.techcup.notifications.service.NotificationService;
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
        CurrentUserProvider.class})
@TestPropertySource(properties = "techcup.security.internal.api-key=test-internal-key")
@org.springframework.boot.context.properties.EnableConfigurationProperties(InternalApiKeyProperties.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void list_withValidJwt_returnsMappedNotifications() throws Exception {
        UUID userId = UUID.randomUUID();
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setType(NotificationType.NUEVO_MENSAJE_CHAT);
        notification.setMessage("hola");
        notification.setRecipientId(userId);
        notification.setCreatedAt(Instant.now());
        when(notificationService.listForUser(eq(userId), eq(null))).thenReturn(List.of(notification));

        mockMvc.perform(get("/api/notificaciones").header("Authorization", "Bearer " + jwtFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].message").value("hola"));
    }

    @Test
    void list_withoutAnyCredentials_isRejected() throws Exception {
        // No credentials at all: Spring Security's anonymous authentication kicks in and
        // the request is denied by the authorization rule itself (403), before ever
        // reaching CurrentUserProvider/GlobalExceptionHandler.
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
        when(notificationService.countUnread(userId)).thenReturn(5L);

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
        when(notificationService.markAsRead(notificationId, userId)).thenReturn(notification);

        mockMvc.perform(patch("/api/notificaciones/{id}/leer", notificationId)
                        .header("Authorization", "Bearer " + jwtFor(userId))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void markAsRead_withoutCsrfToken_isRejected() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        mockMvc.perform(patch("/api/notificaciones/{id}/leer", notificationId)
                        .header("Authorization", "Bearer " + jwtFor(userId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void markAllAsRead_withValidJwt_returnsNoContent() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(patch("/api/notificaciones/leer-todas")
                        .header("Authorization", "Bearer " + jwtFor(userId))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(notificationService).markAllAsRead(userId);
    }

    private static String jwtFor(UUID userId) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("{\"sub\":\"" + userId + "\"}").getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".sig";
    }
}
