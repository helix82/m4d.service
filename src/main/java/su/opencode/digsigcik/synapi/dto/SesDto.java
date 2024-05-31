package su.opencode.digsigcik.synapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SesDto {
    private Long id;
    private Long operationId;
    private String userLogin;
    private String fileHash;
    private String signature;
    private Long sesContainerFileId;
}
