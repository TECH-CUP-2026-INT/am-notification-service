package co.edu.escuelaing.techcup.notifications.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;

class SecurityConfigTest {

    private final SecurityConfig.SpaCsrfTokenRequestHandler requestHandler = new SecurityConfig.SpaCsrfTokenRequestHandler();

    @Test
    void resolveCsrfTokenValue_headerPresent_returnsRawHeaderValue() {
        CsrfToken token = new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "raw-token-value");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(token.getHeaderName(), "raw-token-value");

        String resolved = requestHandler.resolveCsrfTokenValue(request, token);

        assertThat(resolved).isEqualTo("raw-token-value");
    }

    @Test
    void resolveCsrfTokenValue_headerAbsent_decodesXorEncodedParameterValue() {
        CsrfToken token = new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "raw-token-value");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Simula el renderizado en un formulario (p.ej. Thymeleaf leyendo "_csrf.token"):
        // handle() expone la versión ofuscada (XOR) que el navegador reenviaría en el
        // parámetro del formulario, no el valor crudo del token.
        requestHandler.handle(request, response, () -> token);
        CsrfToken renderedToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        request.setParameter(token.getParameterName(), renderedToken.getToken());

        String resolved = requestHandler.resolveCsrfTokenValue(request, token);

        assertThat(resolved).isEqualTo("raw-token-value");
    }

    @Test
    void csrfCookieFilter_tokenPresent_forcesTokenResolutionAndContinuesChain() throws Exception {
        SecurityConfig.CsrfCookieFilter filter = new SecurityConfig.CsrfCookieFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        CsrfToken csrfToken = mock(CsrfToken.class);
        request.setAttribute(CsrfToken.class.getName(), csrfToken);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(csrfToken).getToken();
        verify(chain).doFilter(request, response);
    }

    @Test
    void csrfCookieFilter_tokenAbsent_continuesChainWithoutError() throws Exception {
        SecurityConfig.CsrfCookieFilter filter = new SecurityConfig.CsrfCookieFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
