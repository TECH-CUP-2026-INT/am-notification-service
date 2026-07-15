package co.edu.escuelaing.techcup.notifications.infrastructure.config.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class CurrentUserProviderTest {

    private final CurrentUserProvider provider = new CurrentUserProvider();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserId_authenticatedUserPrincipal_returnsUserId() {
        UUID userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new AuthenticatedUser(userId), null, List.of()));

        assertThat(provider.getCurrentUserId()).isEqualTo(userId);
    }

    @Test
    void getCurrentUserId_noAuthenticationInContext_throwsInsufficientAuthentication() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(provider::getCurrentUserId)
                .isInstanceOf(InsufficientAuthenticationException.class);
    }

    @Test
    void getCurrentUserId_internalServicePrincipal_throwsInsufficientAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new InternalServicePrincipal(), null, List.of()));

        assertThatThrownBy(provider::getCurrentUserId)
                .isInstanceOf(InsufficientAuthenticationException.class);
    }
}
