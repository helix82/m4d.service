package su.opencode.digsigcik.m4d.config.health;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class SSOHealthIndicator implements HealthIndicator {
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;
    private final RestTemplate restTemplate;

    @Override
    public Health health() {
        try {
            String response = restTemplate.getForObject(issuerUri, String.class);
            return Health.up()
                    .withDetail("response", response)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}