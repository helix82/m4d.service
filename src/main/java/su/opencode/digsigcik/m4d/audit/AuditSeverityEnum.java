package su.opencode.digsigcik.m4d.audit;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AuditSeverityEnum {

    INFO("INFO"),
    DEBUG("DEBUG"),
    WARNING("WARNING"),
    ERROR("ERROR");

    @Deprecated
    private final String value;
}
