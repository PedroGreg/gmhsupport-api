package br.com.gmhsupport.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApi {
    @Bean
    public OpenAPI customOpenAPI(@Value("$springdoc.version)") String appVersion){
        return new OpenAPI()
                .info(new Info()
                        .title("GMHSupport")
                        .version(appVersion)
                        .description("")
                        .termsOfService("")
                        .license(new License().name("MIT").url(""))
                        .contact(new Contact()
                                .name("")
                                .url("")
                                .email("")));
    }
}
