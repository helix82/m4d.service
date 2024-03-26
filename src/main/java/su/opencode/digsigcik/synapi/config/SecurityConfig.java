package su.opencode.digsigcik.synapi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final WebConfigProperties webConfigProperties;

    @Order(0)
    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http.cors(c -> c.configurationSource(
                    corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeRequests(rm -> rm
                .antMatchers("/api/**").permitAll()
                .anyRequest().authenticated());
        return http.build();
    }

    // After change, verify that it matches su.opencode.digsigcik.synapi.middleware.ApiCorsFilter.doFilter
    CorsConfigurationSource corsConfigurationSource() {
        var cors = webConfigProperties.getCors();
        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(cors.getAllowedOrigin());
        configuration.setAllowedMethods(cors.getAllowedMethods());
        configuration.setAllowedHeaders(cors.getAllowedHeaders());
        configuration.setExposedHeaders(cors.getExposedHeaders());
//        configuration.setAllowCredentials(true);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
