package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import su.opencode.digsigcik.synapi.enums.PositionEnum;
import su.opencode.digsigcik.synapi.enums.RoleEnum;

//TODO @vitalik.sahnov Временное решение. Удалить после интеграции авторизации
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
    private String login;
    private String password;
    private String firstname;
    private String lastname;
    private String patronymic;
    private PositionEnum position;
    private RoleEnum role;
}
