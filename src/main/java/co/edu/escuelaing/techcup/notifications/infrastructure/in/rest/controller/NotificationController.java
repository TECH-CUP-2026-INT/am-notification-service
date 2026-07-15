package co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.controller;

import co.edu.escuelaing.techcup.notifications.application.mapper.NotificationMapper;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.NotificationUseCase;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.CurrentUserProvider;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.response.NotificationResponse;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.dto.response.UnreadCountResponse;
import co.edu.escuelaing.techcup.notifications.infrastructure.in.rest.swagger.NotificationApi;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final NotificationUseCase notificationUseCase;
    private final CurrentUserProvider currentUserProvider;
    private final NotificationMapper notificationMapper;

    @Override
    @GetMapping
    public List<NotificationResponse> list(@RequestParam(name = "leidas", required = false) Boolean leidas) {
        UUID userId = currentUserProvider.getCurrentUserId();
        return notificationUseCase.listForUser(userId, leidas).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    @GetMapping("/no-leidas/conteo")
    public UnreadCountResponse unreadCount() {
        UUID userId = currentUserProvider.getCurrentUserId();
        return new UnreadCountResponse(notificationUseCase.countUnread(userId));
    }

    @Override
    @PatchMapping("/{id}/leer")
    public NotificationResponse markAsRead(@PathVariable UUID id) {
        UUID userId = currentUserProvider.getCurrentUserId();
        return notificationMapper.toResponse(notificationUseCase.markAsRead(id, userId));
    }

    @Override
    @PatchMapping("/leer-todas")
    public ResponseEntity<Void> markAllAsRead() {
        UUID userId = currentUserProvider.getCurrentUserId();
        notificationUseCase.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
}
