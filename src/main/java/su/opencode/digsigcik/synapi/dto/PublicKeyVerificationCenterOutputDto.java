package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PublicKeyVerificationCenterOutputDto {
    @JsonProperty("id")
    @Schema(description = "ИД шаблона", example = "1", required = false)
    private Long id;

    @Schema(description = "Наименование удостоверяющего центра", example = "Федеральная налоговая служба", required = true)
    @JsonProperty("name")
    private String name;

    @JsonProperty("originalNameRelease")
    private String originalNameRelease;

    @JsonProperty("originalNameRecall")
    private String originalNameRecall;
}
