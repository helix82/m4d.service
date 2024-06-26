package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import su.opencode.digsigcik.synapi.enums.TypeTemplateEnum;
import su.opencode.digsigcik.synapi.enums.VerificationCenterEnum;

@Data
public class PublicKeyTemplateDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("type")
    private TypeTemplateEnum type;

    @JsonProperty("verificationCenter")
    private VerificationCenterEnum verificationCenter;

    @JsonProperty("template")
    private byte[] template;
}
