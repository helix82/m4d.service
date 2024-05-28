package su.opencode.digsigcik.synapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SesXmlDto {
    private Long operationId;
    private String login;
    private String firstName;
    private String lastName;
    private String patronymic;
    private String position;
    private Date timestamp;
    private String signature;
}
