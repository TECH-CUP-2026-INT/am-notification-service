package co.edu.escuelaing.techcup.notifications.infrastructure.config.security;

import co.edu.escuelaing.techcup.notifications.infrastructure.config.InternalApiKeyProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Autentica las llamadas servicio-a-servicio (Sanciones, Comunicaciones, Equipos,
 * Inscripción, Agendamiento) mediante una API key interna compartida, ya que esos
 * endpoints no reciben un JWT de usuario. Si el header no viene o no coincide, no
 * autentica: la cadena de seguridad rechazará la solicitud con 401.
 */
@Component
@RequiredArgsConstructor
public class InternalApiKeyFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Internal-Api-Key";

    private final InternalApiKeyProperties properties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(HEADER_NAME);
        if (apiKey != null && matches(apiKey, properties.apiKey())
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            var authentication = new UsernamePasswordAuthenticationToken(
                    new InternalServicePrincipal(), null, List.of(new SimpleGrantedAuthority("ROLE_SERVICIO_INTERNO")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }

    /** Comparación de tiempo constante para no filtrar la API key por diferencias de latencia. */
    private boolean matches(String received, String expected) {
        if (expected == null) {
            return false;
        }
        return MessageDigest.isEqual(
                received.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
    }
}
