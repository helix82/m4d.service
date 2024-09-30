package su.opencode.digsigcik.m4d.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthConverter jwtAuthConverter;
    private final CorsConfigurationSource corsConfigurationSource;
    private final RPDProperties RPDProperties;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private static final String[] WHITE_LIST = {
            "/v3/api-docs/**",
            "/configuration/ui",
            "/swagger-resources/**",
            "/configuration/security",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/**",
            "/.well-known/acme-challenge/**"
    };

    @Order(1)
    @Bean
    public SecurityFilterChain h2FilterChain(HttpSecurity http) throws Exception {
        http.antMatcher("/h2-console/**")
                .csrf(csrf -> csrf.ignoringRequestMatchers(PathRequest.toH2Console()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toH2Console())
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .headers(headers -> headers.frameOptions().sameOrigin());

        return http.build();
    }

    @Order(2)
    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests()
                .antMatchers(WHITE_LIST)
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthConverter)))
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(customAuthenticationEntryPoint);
        http.cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource))
                .csrf()
                .disable();

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return new JwtIssuerDecoder(RPDProperties.getIssuerUri());
    }
}

