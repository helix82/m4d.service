package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import su.opencode.digsigcik.audit.AllowAudit;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class UserPermissionsResponseDto {
    @AllowAudit
    private Long id;
    @AllowAudit
    private String login;
    @AllowAudit
    private Integer permissions;
    @AllowAudit
    private JsonNode payload;
    @AllowAudit
    private List<String> listTofk;
}
