package br.com.gmhsupport.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApi {
    @Bean
    public OpenAPI customOpenAPI(@Value("$springdoc.version)") String appVersion){
        return new OpenAPI()
                .info(new Info().title("GMHSupport").version("1.0")
                        .description("")
                        .termsOfService("")
                        .license(new License().name("MIT").url(""))
                        .contact(new Contact()
                                .name("")
                                .url("")
                                .email("")))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                // Envia o token em TODOS os endpoints (o /login e o POST /usuario continuam públicos na API)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
