package su.opencode.digsigcik.synapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

//TODO @vitalik.sahnov Временное решение. Удалить после интеграции авторизации
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponseDto {
    private String responseMessage;
    private HttpStatus responseStatus;
    private JwtTokenDto token;
}
