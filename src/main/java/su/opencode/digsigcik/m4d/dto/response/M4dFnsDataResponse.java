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
}
