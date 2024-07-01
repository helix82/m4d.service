package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.enums.TypeTemplateEnum;
import su.opencode.digsigcik.synapi.enums.VerificationCenterEnum;

@Data
public class PublicKeyTemplateDto {
    @JsonProperty("id")
    @Schema(description = "ИД шаблона", example = "1", required = false)
    private Long id;

    @JsonProperty("type")
    @Schema(description = "Тип шаблона", example = "RECALL", required = false)
    private TypeTemplateEnum type;

    @JsonProperty("verificationCenter")
    @Schema(description = "Удостоверяющий центр", example = "FEDERAL_CENTER_INFORMATIZATION", required = false)
    private VerificationCenterEnum verificationCenter;

    @JsonProperty("template")
    @Schema(description = "Файл шаблона", example = "", required = false)
    private MultipartFile template;

    @JsonProperty("contentType")
    @Schema(description = "Тип контента (не заполнять)", example = "", required = false, hidden = true)
    private String contentType;
}
