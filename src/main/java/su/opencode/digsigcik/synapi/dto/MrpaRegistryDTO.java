package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MrpaRegistryDTO {
    @JsonProperty("userId")
    private Long userId;
    @JsonProperty("guid")
    private String guid;
    @JsonProperty("internalNumber")
    private String internalNumber;
    @JsonProperty("organization")
    private String organization;
    @JsonProperty("principal")
    private String principal;
    @JsonProperty("representative")
    private String representative;
    @JsonProperty("startDate")
    private String startDate;
    @JsonProperty("endDate")
    private String endDate;
    @JsonProperty("status")
    private String status;
    @JsonProperty("fitRegistration")
    private String fitRegistration;
    @JsonProperty("sigPath")
    private String sigPath;
    @JsonProperty("mrpaXmlPath")
    private String mrpaXmlPath;
    @JsonProperty("recalledXmlPath")
    private String recalledXmlPath;
    @JsonProperty("credentoials")
    private String credentials;
}
