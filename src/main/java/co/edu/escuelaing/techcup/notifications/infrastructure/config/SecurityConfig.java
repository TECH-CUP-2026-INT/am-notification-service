package co.edu.escuelaing.techcup.notifications.infrastructure.config;

import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.InternalApiKeyFilter;
import co.edu.escuelaing.techcup.notifications.infrastructure.config.security.JwtClaimsFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtClaimsFilter jwtClaimsFilter;
    private final InternalApiKeyFilter internalApiKeyFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // Webhooks de eventos: solo servicio-a-servicio (API key interna),
                        // nunca un JWT de usuario final - de lo contrario cualquier usuario
                        // autenticado podría falsificar eventos de otros servicios.
                        .requestMatchers(HttpMethod.POST,
                                "/api/notificaciones/sanciones",
                                "/api/notificaciones/mensajes",
                                "/api/notificaciones/equipos/**",
                                "/api/notificaciones/inscripciones/**",
                                "/api/notificaciones/partidos")
                        .hasRole("SERVICIO_INTERNO")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtClaimsFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(internalApiKeyFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
