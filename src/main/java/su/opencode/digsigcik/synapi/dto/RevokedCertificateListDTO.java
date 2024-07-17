package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RevokedCertificateListDTO {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("imprint")
    private String imprint;
    @JsonProperty("keyId")
    private String keyId;
    @JsonProperty("issuedTo")
    private String issuedTo;
    @JsonProperty("dateStart")
    private String dateStart;
    @JsonProperty("dateEnd")
    private String dateEnd;
    @JsonProperty("originalFilename")
    private String originalFilename;
}
