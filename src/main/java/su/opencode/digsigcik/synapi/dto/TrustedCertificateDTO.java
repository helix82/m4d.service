package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TrustedCertificateDTO {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("imprint")
    private String imprint;
    @JsonProperty("serialNumber")
    private String serialNumber;
    @JsonProperty("keyId")
    private String keyId;
    @JsonProperty("issuedTo")
    private String issuedTo;
    @JsonProperty("dateStart")
    private String dateStart;
    @JsonProperty("dateEnd")
    private String dateEnd;
    @JsonProperty("certificate")
    private String certificate;
    @JsonProperty("typeInput")
    private String typeInput;
    @JsonProperty("accreditationStatus")
    private String accreditationStatus;
}
