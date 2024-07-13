package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import su.opencode.digsigcik.synapi.enums.TypeUserCertificateEnum;

@Data
public class PublicKeyUserCertificateDto {
    @JsonProperty("id")
    private Long id;
    private String login;
    @JsonProperty("imprint")
    private String imprint;
    @JsonProperty("keyId")
    private String keyId;
    @JsonProperty("serialNumber")
    private String serialNumber;
    @JsonProperty("type")
    private TypeUserCertificateEnum type;
    @JsonProperty("fio")
    private String fio;
    @JsonProperty("inn")
    private String inn;
    @JsonProperty("organization")
    private String organization;
    @JsonProperty("dateStart")
    private String dateStart;
    @JsonProperty("dateEnd")
    private String dateEnd;
    @JsonProperty("dateStartPrivateKey")
    private String dateStartPrivateKey;
    @JsonProperty("dateEndPrivateKey")
    private String dateEndPrivateKey;
    @JsonProperty("issuerWho")
    private String issuerWho;
    @JsonProperty("certificate")
    private String certificate;
    @JsonProperty("statement_id")
    private Long statementId;
}
