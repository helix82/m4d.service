package su.opencode.digsigcik.synapi.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationPropertiesScan(basePackages = "su.opencode.digsigcik.synapi.config")
@EnableConfigurationProperties
@Configuration
public class PropertyConfig {
}
