package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import su.opencode.digsigcik.audit.AllowAudit;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPermissionsRequestDto {
    @AllowAudit
    private String login;
    @AllowAudit
    private Integer permissions;
    @AllowAudit
    private JsonNode payload;
}
