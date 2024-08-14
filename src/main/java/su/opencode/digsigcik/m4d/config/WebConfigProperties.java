package su.opencode.digsigcik.m4d.config;

import lombok.Data;
import lombok.Getter;
import lombok.Value;
import org.modelmapper.ModelMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@DependsOn("propertyConfig")
@ConfigurationProperties(prefix = "web")
@ConstructorBinding
@Value
public class WebConfigProperties {
    private static final String WILDCARD_VALUE = "*";
    private static final List<String> WILDCARD = List.of(WILDCARD_VALUE);

    Cors cors;
    Server server;

    @Getter
    public static class Cors {
        List<String> allowMethods;

        /**
         * A list of allowed request headers.
         * <p>
         * CORS has a whitelisted headers that are alwyas allowed,
         * but specifying all headers is recommended.
         * <p>
         * Authorization header must always be specified explicitly
         * even if the wildcard is used.
         */
        List<String> allowHeaders;
        List<String> exposeHeaders;

        /**
         * Determines how long the preflight response can be cached
         * on the client, in seconds.
         */
        Long maxAge;

        public Cors(List<String> allowMethods,
                    List<String> allowHeaders,
                    List<String> exposeHeaders,
                    Long maxAge) {
            if (allowMethods == null) {
                allowMethods = WILDCARD;
            }
            if (allowHeaders == null) {
                allowHeaders = List.of(HttpHeaders.AUTHORIZATION, WILDCARD_VALUE);
            }
            // There's no missing "if"; don't set exposed headers by default.
            if (maxAge == null) {
                maxAge = 3600L;
            }

            this.allowMethods = allowMethods;
            this.allowHeaders = allowHeaders;
            this.exposeHeaders = exposeHeaders;
            this.maxAge = maxAge;
        }
    }

    @Data
    public static class Server {
        String uri;
    }

    @Bean
    public RestTemplate restTemplateBean() { return new RestTemplate(); }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
