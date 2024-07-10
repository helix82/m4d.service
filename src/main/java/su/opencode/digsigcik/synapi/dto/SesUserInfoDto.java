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
public class SesUserInfoDto {
    private String hashAlgorithmType;
    private String hashSignatureAlgorithm;
    private String pinCodeHashAlgorithm;
    private String firstname;
    private String lastname;
    private String patronymic;
    private LocalDateTime timestamp;
}
