package co.edu.escuelaing.techcup.notifications.security;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.escuelaing.techcup.notifications.config.InternalApiKeyProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class InternalApiKeyFilterTest {

    private static final String API_KEY = "test-internal-key";

    private final InternalApiKeyFilter filter = new InternalApiKeyFilter(new InternalApiKeyProperties(API_KEY));

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_matchingApiKey_authenticatesAsInternalServicePrincipal() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(InternalApiKeyFilter.HEADER_NAME, API_KEY);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isInstanceOf(InternalServicePrincipal.class);
        assertThat(authentication.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_SERVICIO_INTERNO");
    }

    @Test
    void doFilter_missingApiKeyHeader_doesNotAuthenticate() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_wrongApiKey_doesNotAuthenticate() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(InternalApiKeyFilter.HEADER_NAME, "wrong-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_alreadyAuthenticated_doesNotOverwriteExistingAuthentication() throws Exception {
        AuthenticatedUser existingPrincipal = new AuthenticatedUser(java.util.UUID.randomUUID());
        Authentication existing = new UsernamePasswordAuthenticationToken(existingPrincipal, null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(existing);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(InternalApiKeyFilter.HEADER_NAME, API_KEY);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existing);
    }
}
