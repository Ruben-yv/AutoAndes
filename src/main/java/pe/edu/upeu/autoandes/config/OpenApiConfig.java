package pe.edu.upeu.autoandes.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "AutoAndes API",
                version = "v1",
                description = "API REST para la gestión de servicios, vehículos y órdenes de servicio del taller AutoAndes."
        )
)
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer bearerAuthorization() {
        return openApi -> {
            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }
            openApi.getComponents().addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("Pega el token JWT recibido desde POST /api/v1/auth/login."));

            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().forEach((path, pathItem) -> pathItem.readOperations().forEach(operation -> {
                boolean loginPublico = "/api/v1/auth/login".equals(path)
                        && pathItem.getPost() == operation;
                if (!loginPublico) {
                    operation.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
                }
            }));
        };
    }
}
