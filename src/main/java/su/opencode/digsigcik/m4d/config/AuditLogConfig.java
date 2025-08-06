package su.opencode.digsigcik.m4d.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import su.opencode.digsigcik.audit.AuditLogRestService;
import su.opencode.digsigcik.audit.AuditLogRestSupport;
import su.opencode.digsigcik.audit.aop.HealthCheckLoggingAspect;

@Configuration
public class AuditLogConfig {

    @Bean
    public AuditLogRestService auditLogRestService() {
        return new AuditLogRestService();
    }

    @Bean
    public AuditLogRestSupport auditLogRestSupport(AuditLogRestService auditLogRestService) {
        return new AuditLogRestSupport(auditLogRestService);
    }

    @Bean
    public HealthCheckLoggingAspect healthCheckLoggingAspect(AuditLogRestService auditLogRestService) {
        return new HealthCheckLoggingAspect("M4D", auditLogRestService);
    }

}
