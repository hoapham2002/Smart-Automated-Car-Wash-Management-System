package com.autowash.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * B19 - global OpenAPI/Swagger metadata (title, description, JWT auth scheme)
 * so /swagger-ui.html shows a proper "Authorize" button for all 🔒 endpoints
 * instead of just a flat, unlabeled list of controllers.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI autowashOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AutoWash Pro API")
                        .description("Car wash management + loyalty program backend "
                                + "(Auth, Booking, Wash, Payment, Loyalty, Promotion, Research)")
                        .version("v1")
                        .contact(new Contact().name("AutoWash Pro Team")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .name(BEARER_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
