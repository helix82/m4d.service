package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class M4dRequestSaveWithCredentialDto {
    @JsonProperty("organisationId")
    private Long organisationId;

    @JsonProperty("principalId")
    private Long principalId;

    @JsonProperty("representativeId")
    private Long representativeId;

    @JsonProperty("startDate")
    private String startDate;

    @JsonProperty("endDate")
    private String endDate;

    @JsonProperty("credentials")
    private Long[] credentials;
}
