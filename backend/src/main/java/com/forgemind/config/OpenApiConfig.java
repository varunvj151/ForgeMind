package com.forgemind.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) documentation configuration.
 *
 * <p>Registers a JWT Bearer security scheme definition so that once Phase 2
 * authentication is implemented, every secured endpoint can be tested directly
 * from the Swagger UI by pasting a token.
 *
 * <p>Swagger UI available at: {@code /swagger-ui.html}
 * <p>OpenAPI JSON spec at:   {@code /api-docs}
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.env:development}")
    private String appEnv;

    @Bean
    public OpenAPI forgemindOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("ForgeMind API")
                        .description("ForgeMind — AI Software Engineering Platform REST API")
                        .version("v1")
                        .contact(new Contact()
                                .name("ForgeMind Team")
                                .email("dev@forgemind.io"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://forgemind.io/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development"),
                        new Server()
                                .url("https://api.forgemind.io")
                                .description("Production")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste a JWT access token obtained from POST /api/v1/auth/login")));
    }
}
