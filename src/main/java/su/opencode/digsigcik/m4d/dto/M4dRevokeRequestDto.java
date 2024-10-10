package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import su.opencode.digsigcik.m4d.enums.AuthorTypeEnum;

@Data
public class M4dRevokeRequestDto {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("mrpaRegistryId")
    private Long mrpaRegistryId;
    @JsonProperty("author")
    private AuthorTypeEnum author;
    @JsonProperty("argument")
    private String argument;
    @JsonProperty("signature")
    private String signature;
}
