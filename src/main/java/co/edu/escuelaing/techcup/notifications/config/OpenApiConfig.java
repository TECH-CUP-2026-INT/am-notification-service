package co.edu.escuelaing.techcup.notifications.config;

import co.edu.escuelaing.techcup.notifications.security.InternalApiKeyFilter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";
    private static final String INTERNAL_API_KEY_SCHEME = "internalApiKey";

    @Bean
    public OpenAPI notificationServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Servicio de Notificaciones - TechCup Fútbol")
                        .description("Consumidor de eventos de otros microservicios y productor de "
                                + "alertas in-app para el usuario final.")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .addSecurityItem(new SecurityRequirement().addList(INTERNAL_API_KEY_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT ya validado por el API Gateway. Debe incluir el claim "
                                        + "\"sub\" con el UUID del usuario. Solo para los endpoints de "
                                        + "consulta del usuario final."))
                        .addSecuritySchemes(INTERNAL_API_KEY_SCHEME, new SecurityScheme()
                                .name(InternalApiKeyFilter.HEADER_NAME)
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("API key interna compartida entre microservicios. Solo para "
                                        + "los endpoints que reciben eventos de otros servicios.")));
    }
}
