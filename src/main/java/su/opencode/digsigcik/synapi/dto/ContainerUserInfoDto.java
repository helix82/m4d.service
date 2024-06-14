package su.opencode.digsigcik.synapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
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
