package com.aivideoeraser.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("AI Video Eraser - Backend Server API")
                        .version("1.0.0")
                        .description("Production-Ready Backend Server powering AI Video Eraser (iOS App ID: 6744909217, Android, and Web Clients). " +
                                "Provides complete RESTful APIs for video uploading, AI object removal/inpainting (ProPainter, E2FGVI, FastInpaint), " +
                                "AI object tracking, custom mask tracking (rectangles, ellipses, freehand brush), user quotas/credits, subscriptions, and GPU task queue management.")
                        .contact(new Contact()
                                .name("AI Video Eraser Engineering Team")
                                .email("support@aivideoeraser.app"))
                        .license(new License().name("Proprietary").url("https://aivideoeraser.app/terms")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
