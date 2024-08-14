package su.opencode.digsigcik.m4d.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationPropertiesScan(basePackages = "su.opencode.digsigcik.m4d.config")
@EnableConfigurationProperties
@Configuration
public class PropertyConfig {
}
