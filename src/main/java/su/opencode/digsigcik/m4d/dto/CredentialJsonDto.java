package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import su.opencode.digsigcik.audit.AllowAudit;

@Data
public class CredentialJsonDto {
    @JsonProperty("code")
    @AllowAudit
    private String code;
    @JsonProperty("description")
    @AllowAudit
    private String description;
    @JsonProperty("type")
    @AllowAudit
    private String type;

    @JsonProperty("group")
    @AllowAudit
    public Group group;

    @Data
    public static class Group {
        @JsonProperty("code")
        @AllowAudit
        private String code;
        @JsonProperty("description")
        @AllowAudit
        private String description;
    }
}
