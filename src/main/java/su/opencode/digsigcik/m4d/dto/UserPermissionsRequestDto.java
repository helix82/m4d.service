package su.opencode.digsigcik.m4d.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPermissionsRequestDto {
    private String login;
    private Integer permissions;
    private String payload;
}
