package su.opencode.digsigcik.synapi.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import su.opencode.digsigcik.synapi.auth.validation.annotation.Password;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtRequestDto {
    private String username;
    @Password
    private String password;
}
