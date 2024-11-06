package su.opencode.digsigcik.m4d.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import su.opencode.digsigcik.audit.AuditLogRestService;
import su.opencode.digsigcik.audit.AuditLogRestSupport;

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

}
