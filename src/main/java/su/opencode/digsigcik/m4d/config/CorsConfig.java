package su.opencode.digsigcik.m4d.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class CorsConfig {
    private final CorsProperties corsProperties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // TODO: потом настроить ориджины скорее всего надо
        config.setAllowedOriginPatterns(corsProperties.getAllowOrigin());
        config.setAllowedMethods(corsProperties.getAllowMethods());
        config.setAllowedHeaders(corsProperties.getAllowHeaders());
        config.setMaxAge(Duration.ofMinutes(corsProperties.getMaxAge()));
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}