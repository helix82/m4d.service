package su.opencode.digsigcik.m4d.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import su.opencode.digsigcik.specification.service.SpecificationService;
import su.opencode.digsigcik.specification.service.SpecificationServiceImpl;

@Configuration
public class SpecificationConfig {

    // Включение механизма пэйджинации

    @Bean
    public SpecificationService specificationService() {
        return new SpecificationServiceImpl();
    }
}
