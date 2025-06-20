package su.opencode.digsigcik.m4d.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class M4dResponseInfo {
    @JsonProperty("poaId")
    private String poaId; // хэш доверенности
    @JsonProperty("poaNumber")
    private String poaNumber; // guid доверенности
    @JsonProperty("issuerInn")
    private String issuerInn; // инн доверителя
    @JsonProperty("txId")
    private String txId; // номер транзакции
    @JsonProperty("error")
    private Error error;

    @Data
    public static class Error {
        @JsonProperty("detail")
        private String detail; // текст ошибки
        @JsonProperty("instance")
        private String instance; // URL, идентифицирующий ошибку
        @JsonProperty("meta")
        private String meta; // Метаданные ошибки
        @JsonProperty("status")
        private Integer status; // HTTP статус
        @JsonProperty("title")
        private String title; // заголовок ошибки
        @JsonProperty("type")
        private String type; // код ошибки
        @JsonProperty("dictionary_code")
        private String dictionary_code; // Код ошибки в справочнике ERROR-кодов
    }

}

