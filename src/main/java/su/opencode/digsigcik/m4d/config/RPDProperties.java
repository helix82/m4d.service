package su.opencode.digsigcik.m4d.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class RPDProperties {
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;
    @Value("${spring.security.oauth2.client.provider.rpd.authorization-uri}")
    private String authorizationUri;
    @Value("${spring.security.oauth2.client.registration.rpd.client-id}")
    private String clientId;
}
