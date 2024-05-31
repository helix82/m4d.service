package su.opencode.digsigcik.synapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//TODO @vitalik.sahnov Временное решение. Удалить после интеграции авторизации
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponseErrorDto {
    private String message;
    private Integer code;
}
