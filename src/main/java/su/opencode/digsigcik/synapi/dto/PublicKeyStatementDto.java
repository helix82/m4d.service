package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import su.opencode.digsigcik.synapi.enums.StatusStatementEnum;
import su.opencode.digsigcik.synapi.enums.TypeStatementEnum;

@Data
public class PublicKeyStatementDto {
    @JsonProperty("id")
    @Schema(description = "ИД заявления", example = "1", required = true)
    private Long id;

    @JsonProperty("login")
    @Schema(description = "Логин пользователя", example = "IvanovII", required = false)
    private String login;

    @JsonProperty("dtCreate")
    @Schema(description = "Дата создания заявления", example = "2024-06-10T21:00:00Z", required = false)
    private String dtCreate;

    @JsonProperty("verificationCenter")
    @Schema(description = "Наименование удостоверяющего центра", example = "Федеральный центр информатизации", required = false)
    private String verificationCenter;

    @JsonProperty("verificationCenterId")
    @Schema(description = "ИД удостоверяющего центра", example = "1", required = false)
    private String verificationCenterId;

    @JsonProperty("type")
    @Schema(description = "Тип заявления", example = "RELEASE", required = false)
    private TypeStatementEnum type;

    @JsonProperty("status")
    @Schema(description = "Статус заявления", example = "IN_PROGRESS", required = false)
    private StatusStatementEnum status;
}
