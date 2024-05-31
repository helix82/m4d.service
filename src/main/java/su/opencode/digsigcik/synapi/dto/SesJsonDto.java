package su.opencode.digsigcik.synapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import su.opencode.digsigcik.synapi.enums.PositionEnum;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SesJsonDto {
    private Long operationId;
    private String preferredUsername;
    private String firstName;
    private String lastName;
    private String patronymic;
    private PositionEnum position;
    private String timestamp;
    private String signature;
}
