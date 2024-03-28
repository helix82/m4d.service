package su.opencode.digsigcik.synapi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    public static final String CORS_CFG_SOURCE_BEAN = "corsConfigurationSource";

    private final WebConfigProperties webConfigProperties;

    @Order(0)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeRequests(rm -> rm
                .antMatchers("/api/**").permitAll()
                .anyRequest().authenticated());
        return http.build();
    }

    @Bean
    public CorsProcessor corsProcessor() {
        return new DefaultCorsProcessor();
    }

    @Bean(CORS_CFG_SOURCE_BEAN)
    public CorsConfigurationSource corsConfigurationSource() {
        var cors = webConfigProperties.getCors();
        var configuration = new CorsConfiguration();
        configuration.setAllowedMethods(cors.getAllowMethods());
        configuration.setAllowedHeaders(cors.getAllowHeaders());
        configuration.setExposedHeaders(cors.getExposeHeaders());
        configuration.setAllowCredentials(true);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
