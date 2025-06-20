package su.opencode.digsigcik.m4d.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import su.opencode.digsigcik.audit.AllowAudit;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class M4dFnsDataResponse {
    @JsonProperty("poaNumber")
    @AllowAudit
    private String guid;
    @JsonProperty("id")
    @AllowAudit
    private String id;
    @JsonProperty("createdDate")
    @AllowAudit
    private String createdDate;
    @JsonProperty("createdDateTime")
    @AllowAudit
    private String createdDateTime;
    @JsonProperty("startDate")
    @AllowAudit
    private String startDate;
    @JsonProperty("startDateTime")
    @AllowAudit
    private String startDateTime;
    @JsonProperty("endDate")
    @AllowAudit
    private String endDate;
    @JsonProperty("endDateTime")
    @AllowAudit
    private String endDateTime;
    @JsonProperty("statusDate")
    @AllowAudit
    private String statusDate;
    @JsonProperty("statusDateTime")
    @AllowAudit
    private String statusDateTime;
    @JsonProperty("status")
    @AllowAudit
    private String status;
    @JsonProperty("issuerPublicKey")
    @AllowAudit
    private String issuerPublicKey;
    @JsonProperty("issuerSnilsHash")
    @AllowAudit
    private String issuerSnilsHash;
    @JsonProperty("issuerOgrnHash")
    @AllowAudit
    private String issuerOgrnHash;
    @JsonProperty("error")
    @AllowAudit
    private Error error;

    @Data
    public static class Error {
        @JsonProperty("detail")
        @AllowAudit
        private String detail; // текст ошибки
        @JsonProperty("instance")
        @AllowAudit
        private String instance; // URL, идентифицирующий ошибку
        @JsonProperty("meta")
        @AllowAudit
        private String meta; // Метаданные ошибки
        @JsonProperty("status")
        @AllowAudit
        private Integer status; // HTTP статус
        @JsonProperty("title")
        @AllowAudit
        private String title; // заголовок ошибки
        @JsonProperty("type")
        @AllowAudit
        private String type; // код ошибки
        @JsonProperty("dictionary_code")
        @AllowAudit
        private String dictionary_code; // Код ошибки в справочнике ERROR-кодов
    }
}
