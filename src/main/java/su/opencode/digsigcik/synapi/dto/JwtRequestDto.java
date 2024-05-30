package su.opencode.digsigcik.synapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import su.opencode.digsigcik.synapi.validation.annotation.Password;

//TODO @vitalik.sahnov Временное решение. Удалить после интеграции авторизации
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtRequestDto {
    private String login;
    @Password
    private String password;
}
