package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SesDto {
    private Long id;
    private String userLogin;
    private String fileHash;
    private String signature;
    private String operationDate;
    private Long containerId;
    private Long sesContainerFileId;
}
