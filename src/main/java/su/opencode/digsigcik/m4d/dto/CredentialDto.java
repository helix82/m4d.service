package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import su.opencode.digsigcik.audit.AllowAudit;

@Data
public class CredentialDto {
    @JsonProperty("id")
    @AllowAudit
    private Long id;

    @JsonProperty("name")
    @AllowAudit
    private String name;

    @JsonProperty("code")
    @AllowAudit
    private String code;
}
