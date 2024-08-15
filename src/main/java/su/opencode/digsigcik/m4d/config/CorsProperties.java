package su.opencode.digsigcik.m4d.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "web.cors")
@Data
public class CorsProperties {
    private List<String> allowOrigin;
    private List<String> allowMethods;
    private List<String> allowHeaders;
    private Long maxAge;
}
