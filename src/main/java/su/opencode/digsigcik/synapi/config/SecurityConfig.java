package su.opencode.digsigcik.synapi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import su.opencode.digsigcik.synapi.filters.JwtRequestFilter;
import su.opencode.digsigcik.synapi.service.UserService;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

//TODO @vitalik.sahnov Временное решение. Удалить после интеграции авторизации
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    public static final String CORS_CFG_SOURCE_BEAN = "corsConfigurationSource";
    private final WebConfigProperties webConfigProperties;
    private final UserService userService;
    private final JwtRequestFilter jwtRequestFilter;


    @Order(0)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                //TODO: remove h2 console start
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(toH2Console())
                        .disable()
                )
                //TODO: remove h2 console finish
                .authorizeRequests(rm -> rm
                        .antMatchers("/api/**")
                        .permitAll()
                        .antMatchers("/v2/api-docs",
                                "/configuration/ui",
                                "/swagger-resources/**",
                                "/configuration/security",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/.well-known/acme-challenge/**",
                                //TODO: remove h2 console start
                                "/h2-console",
                                "/h2-console/**"
                                //TODO: remove h2 console finish
                        )
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                        .and())
                //TODO: remove h2 console start
                .headers(headers -> headers.frameOptions()
                        .disable())
                //TODO: remove h2 console finish
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .and()
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(userService);
        return daoAuthenticationProvider;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
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
    //TODO: remove h2 console start
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .antMatchers("/h2-console")
                .antMatchers("/h2-console/**");
    }
    //TODO: remove h2 console finish
}
