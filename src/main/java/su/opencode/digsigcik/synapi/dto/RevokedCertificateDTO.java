package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class RevokedCertificateDTO {
    @JsonProperty("certCentrNameId")
    private Long certCentrNameId;
    @JsonProperty("dateStart")
    private Timestamp dateStart;
    @JsonProperty("dateEnd")
    private Timestamp dateEnd;
    @JsonProperty("typeOfAddition")
    private String typeOfAddition;
    @JsonProperty("certCenterType")
    private String certCenterType;
    @JsonProperty("certificateData")
    private String certificateData;
}
