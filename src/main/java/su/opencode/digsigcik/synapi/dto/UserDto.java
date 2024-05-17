package su.opencode.digsigcik.synapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import su.opencode.digsigcik.synapi.enums.PositionEnum;
import su.opencode.digsigcik.synapi.enums.RoleEnum;
//TODO @vitalik.sahnov Временное решение. Удалить после интеграции авторизации
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String login;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String patronymic;
    private PositionEnum position;
    private RoleEnum role;
}
