package su.opencode.digsigcik.synapi.config;

import lombok.Getter;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.DependsOn;

import java.util.List;

@DependsOn("propertyConfig")
@ConfigurationProperties(prefix = "web")
@ConstructorBinding
@Value
public class WebConfigProperties {
    private static final String WILDCARD_VALUE = "*";
    private static final List<String> WILDCARD = List.of(WILDCARD_VALUE);

    Cors cors;

    @Getter
    public static class Cors {
        /**
         * A list of allowed origins.
         *
         * A wildcard can be used here, but it is incompatible
         * with Access-Control-Allow-Credentials being "true",
         * which must be "true" in all cases where auth data must
         * be sent within the headers of a CORS request.
         */
        List<String> allowedOrigin;
        String allowedOriginValue;
        boolean wildcardAllowedOrigin;

        List<String> allowedMethods;
        String allowedMethodsValue;

        /**
         * A list of allowed request headers.
         *
         * CORS has a whitelisted headers that are alwyas allowed,
         * but specifying all headers is recommended.
         *
         * Authorization header must always be specified explicitly
         * even if the wildcard is used.
         */
        List<String> allowedHeaders;
        String allowedHeadersValue;

        List<String> exposedHeaders;
        String exposedHeadersValue;

        /**
         * Determines how long the preflight response can be cached
         * on the client, in seconds.
         */
        Long maxAge;
        String maxAgeValue;

        public Cors(List<String> allowedOrigin,
                    List<String> allowedMethods,
                    List<String> allowedHeaders,
                    List<String> exposedHeaders,
                    Long maxAge) {
            if (allowedOrigin == null) {
                allowedOrigin = List.of(
                        "https://localhost:*",
                        "http://localhost:*");
            } else {
                for (var o : allowedOrigin) {
                    if (o.equals(WILDCARD_VALUE)) {
                        this.wildcardAllowedOrigin = true;
                        break;
                    }
                }
                if (wildcardAllowedOrigin && allowedOrigin.size() > 1) {
                    throw new IllegalArgumentException(
                            "web.cors.allowed-origins must not specify the " +
                            "\"*\" wildcard along with specific origins");
                }
            }
            if (allowedMethods == null) {
                allowedMethods = WILDCARD;
            }
            if (allowedHeaders == null) {
                allowedHeaders = WILDCARD;
            }
            // There's no missing "if"; don't set exposed headers by default.
            if (maxAge == null) {
                maxAge = 3600L;
            }

            this.allowedOrigin = allowedOrigin;
            this.allowedMethods = allowedMethods;
            this.allowedHeaders = allowedHeaders;
            this.exposedHeaders = exposedHeaders;
            this.maxAge = maxAge;

            this.allowedOriginValue = String.join(", ", allowedOrigin);
            this.allowedMethodsValue = String.join(", ", allowedMethods);
            this.allowedHeadersValue = String.join(", ", allowedHeaders);
            if (exposedHeadersValue != null) {
                this.exposedHeadersValue = String.join(", ", exposedHeaders);
            }
            this.maxAgeValue = String.valueOf(maxAge);
        }
    }
}
