package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CredentialDTO {
    @JsonProperty("name")
    private String name;
    @JsonProperty("code")
    private String code;
}
