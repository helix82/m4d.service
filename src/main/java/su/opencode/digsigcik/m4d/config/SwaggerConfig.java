package su.opencode.digsigcik.m4d.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Dev-Team",
                        url = "https://www.o-code.ru/",
                        email = "vereschagina@o-code.ru"

                ),
                title = "API Documentation",
                version = "3.0"
        ),
        security = {
                @SecurityRequirement(
                        name = "bearerAuth"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT auth description",
        scheme = "bearer",
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(
                authorizationCode =
                @OAuthFlow(
                        authorizationUrl = "${spring.security.oauth2.client.provider.rpd.authorization-uri}",
                        tokenUrl = "${springdoc.server.token-uri}"


                )
        ),
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {
    @Value("${springdoc.server.url}")
    private String url;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(Collections.singletonList(
                        new Server().url(url).description("Default Server URL")
                ));
    }
}
