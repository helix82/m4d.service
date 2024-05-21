package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class CertificateDTO {
    @JsonProperty("certCentrNameId")
    private Long certCentrNameId;
    @JsonProperty("validityPeriod")
    private Timestamp validityPeriod;
    @JsonProperty("typeOfAddition")
    private String typeOfAddition;
    @JsonProperty("certCenterType")
    private String certCenterType;
    @JsonProperty("filePath")
    private String filePath;
}
