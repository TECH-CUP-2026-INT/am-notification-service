package co.edu.escuelaing.techcup.notifications.security;

import co.edu.escuelaing.techcup.notifications.config.InternalApiKeyProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
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
public class InternalApiKeyFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Internal-Api-Key";

    private final InternalApiKeyProperties properties;

    public InternalApiKeyFilter(InternalApiKeyProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(HEADER_NAME);
        if (apiKey != null && apiKey.equals(properties.apiKey())
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            var authentication = new UsernamePasswordAuthenticationToken(
                    new InternalServicePrincipal(), null, List.of(new SimpleGrantedAuthority("ROLE_SERVICIO_INTERNO")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }
}
