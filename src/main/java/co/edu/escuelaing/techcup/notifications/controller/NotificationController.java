package co.edu.escuelaing.techcup.notifications.controller;

import co.edu.escuelaing.techcup.notifications.dto.response.NotificationResponse;
import co.edu.escuelaing.techcup.notifications.dto.response.UnreadCountResponse;
import co.edu.escuelaing.techcup.notifications.mapper.NotificationMapper;
import co.edu.escuelaing.techcup.notifications.security.CurrentUserProvider;
import co.edu.escuelaing.techcup.notifications.service.NotificationService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API consultada por el frontend (estilo campanita): historial de notificaciones del
 * usuario autenticado, conteo de no leídas y marcado de leídas.
 */
@RestController
@RequestMapping("/api/notificaciones")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserProvider currentUserProvider;

    public NotificationController(NotificationService notificationService, CurrentUserProvider currentUserProvider) {
        this.notificationService = notificationService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public List<NotificationResponse> list(@RequestParam(name = "leidas", required = false) Boolean leidas) {
        UUID userId = currentUserProvider.getCurrentUserId();
        return notificationService.listForUser(userId, leidas).stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }

    @GetMapping("/no-leidas/conteo")
    public UnreadCountResponse unreadCount() {
        UUID userId = currentUserProvider.getCurrentUserId();
        return new UnreadCountResponse(notificationService.countUnread(userId));
    }

    @PatchMapping("/{id}/leer")
    public NotificationResponse markAsRead(@PathVariable UUID id) {
        UUID userId = currentUserProvider.getCurrentUserId();
        return NotificationMapper.toResponse(notificationService.markAsRead(id, userId));
    }

    @PatchMapping("/leer-todas")
    public ResponseEntity<Void> markAllAsRead() {
        UUID userId = currentUserProvider.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
}
