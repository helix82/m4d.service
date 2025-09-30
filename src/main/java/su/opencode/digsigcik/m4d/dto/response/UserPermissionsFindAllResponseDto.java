package su.opencode.digsigcik.m4d.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class UserPermissionsFindAllResponseDto {
    private Long id;
    private String login;
    private Integer permissions;
    private JsonNode payload;
}
