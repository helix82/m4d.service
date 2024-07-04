package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PublicKeyVerificationCenterInputDto {
    @JsonProperty("id")
    @Schema(description = "ИД шаблона", example = "1", required = false)
    private Long id;

    @Schema(description = "Наименование удостоверяющего центра", example = "Федеральная налоговая служба", required = true)
    @JsonProperty("name")
    private String name;

    @Schema(description = "Шаблон заявления на выпуск", example = "", required = false)
    @JsonProperty("templateRelease")
    private MultipartFile templateRelease;

    @Schema(description = "Шаблон заявления на отзыв", example = "", required = false)
    @JsonProperty("templateRecall")
    private MultipartFile templateRecall;
}
