package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import su.opencode.digsigcik.synapi.enums.TypeUserCertificateEnum;

@Data
public class PublicKeyUserCertificateDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("login")
    private String login;

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

    @JsonProperty("dateStart")
    private String dateStart;

    @JsonProperty("dateEnd")
    private String dateEnd;

    @JsonProperty("issuedWho")
    private String issuedWho;

    @JsonProperty("certificate")
    private byte[] certificate;
}
