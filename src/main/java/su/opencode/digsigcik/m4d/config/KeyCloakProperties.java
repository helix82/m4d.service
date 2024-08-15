package su.opencode.digsigcik.m4d.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class KeyCloakProperties {
    @Value("${spring.security.oauth2.resourceserver.issuers.public-uri}")
    private String issuerPublicUri;
    @Value("${spring.security.oauth2.resourceserver.issuers.internal-uri}")
    private String issuerInternalUri;
}
