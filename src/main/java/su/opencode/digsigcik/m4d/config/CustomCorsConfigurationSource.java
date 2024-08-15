package su.opencode.digsigcik.m4d.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class CustomCorsConfigurationSource implements CorsConfigurationSource {
    private final CorsProperties corsProperties;

    @Override
    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(corsProperties.getAllowOrigin());
        config.setAllowedMethods(corsProperties.getAllowMethods());
        config.setAllowedHeaders(corsProperties.getAllowHeaders());
        config.setMaxAge(Duration.ofMinutes(corsProperties.getMaxAge()));
        return config;
    }
}
