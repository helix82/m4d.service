package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class UserPermissionsResponseDto {
    private Long id;
    private String login;
    private Integer permissions;
    private JsonNode payload;
    private List<String> listTofk;
}
