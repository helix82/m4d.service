package su.opencode.digsigcik.synapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import su.opencode.digsigcik.synapi.enums.PositionEnum;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SesResponseDto {
    private String containerUUID;
    private String firstName;
    private String lastName;
    private String patronymic;
    private PositionEnum position;
    private String principalFullName;
    private String organisation;
    private String sigEnhancementType;
    private String sigType;
}
