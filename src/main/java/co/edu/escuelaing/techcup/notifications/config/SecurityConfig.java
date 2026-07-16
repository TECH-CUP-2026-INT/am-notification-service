package co.edu.escuelaing.techcup.notifications.config;

import co.edu.escuelaing.techcup.notifications.security.InternalApiKeyFilter;
import co.edu.escuelaing.techcup.notifications.security.JwtClaimsFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // Webhooks servicio-a-servicio: los origina otro backend, nunca el navegador de
    // un usuario, así que no hay "víctima" a la que un ataque CSRF pueda engañar (y
    // esos llamadores tampoco pueden obtener la cookie CSRF, porque no navegan el
    // sitio). Se excluyen de CSRF; siguen protegidos por InternalApiKeyFilter.
    private static final String[] SERVICE_TO_SERVICE_PATHS = {
            "/api/notificaciones/sanciones",
            "/api/notificaciones/sanciones-conducta",
            "/api/notificaciones/mensajes",
            "/api/notificaciones/equipos/**",
            "/api/notificaciones/inscripciones/**",
            "/api/notificaciones/partidos"
    };

    private final JwtClaimsFilter jwtClaimsFilter;
    private final InternalApiKeyFilter internalApiKeyFilter;

    public SecurityConfig(JwtClaimsFilter jwtClaimsFilter, InternalApiKeyFilter internalApiKeyFilter) {
        this.jwtClaimsFilter = jwtClaimsFilter;
        this.internalApiKeyFilter = internalApiKeyFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        RequestMatcher[] serviceToServiceMatchers = new RequestMatcher[SERVICE_TO_SERVICE_PATHS.length];
        for (int i = 0; i < SERVICE_TO_SERVICE_PATHS.length; i++) {
            serviceToServiceMatchers[i] = new AntPathRequestMatcher(SERVICE_TO_SERVICE_PATHS[i], "POST");
        }

        http
                // Token CSRF en cookie legible por JS (no en sesión: el servicio es
                // STATELESS). El frontend debe leer la cookie "XSRF-TOKEN" y reenviar su
                // valor en el header "X-XSRF-TOKEN" en cada POST/PUT/PATCH/DELETE.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                        .ignoringRequestMatchers(serviceToServiceMatchers))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // Webhooks de eventos: solo servicio-a-servicio (API key interna),
                        // nunca un JWT de usuario final - de lo contrario cualquier usuario
                        // autenticado podría falsificar eventos de otros servicios.
                        .requestMatchers(HttpMethod.POST, SERVICE_TO_SERVICE_PATHS).hasRole("SERVICIO_INTERNO")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtClaimsFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(internalApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                // El CsrfFilter difiere la generación del token hasta que algo lo lea
                // (para no pagar el costo en cada request); en una API JSON pura nada lo
                // lee nunca, así que forzamos la lectura aquí para que la cookie
                // efectivamente se escriba en la respuesta.
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);
        return http.build();
    }

    static final class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {
        private final CsrfTokenRequestHandler delegate = new XorCsrfTokenRequestAttributeHandler();

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
            delegate.handle(request, response, csrfToken);
        }

        @Override
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
            String headerValue = request.getHeader(csrfToken.getHeaderName());
            return StringUtils.hasText(headerValue)
                    ? super.resolveCsrfTokenValue(request, csrfToken)
                    : delegate.resolveCsrfTokenValue(request, csrfToken);
        }
    }

    static final class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                csrfToken.getToken();
            }
            filterChain.doFilter(request, response);
        }
    }
}
