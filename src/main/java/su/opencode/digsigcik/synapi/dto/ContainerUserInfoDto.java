package su.opencode.digsigcik.synapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerUserInfoDto {
    private String login;
    private String firstname;
    private String lastname;
    private String patronymic;
    private LocalDateTime timestamp;
}
