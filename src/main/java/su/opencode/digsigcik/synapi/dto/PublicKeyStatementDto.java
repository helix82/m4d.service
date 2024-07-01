package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import su.opencode.digsigcik.synapi.enums.StatusStatementEnum;

@Data
public class PublicKeyStatementDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("login")
    private String login;

    @JsonProperty("dtCreate")
    private String dtCreate;

    @JsonProperty("templateId")
    private String templateId;

    @JsonProperty("status")
    private StatusStatementEnum status;
}
