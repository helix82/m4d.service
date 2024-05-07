package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

@Getter
@Setter
public class MrpaRegistryDTO {
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
    private DateTime startDate;
    @JsonProperty("endDate")
    private DateTime endDate;
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
