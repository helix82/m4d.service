package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import su.opencode.digsigcik.synapi.enums.FtxRegistrationTypeEnum;
import su.opencode.digsigcik.synapi.enums.StatusTypeEnum;

@Data
public class MrpaRegistryWithFioDTO {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("guid")
    private String guid;
    @JsonProperty("internalNumber")
    private String internalNumber;
    @JsonProperty("organizationId")
    private Long organizationId;
    @JsonProperty("principalId")
    private Long principalId;
    @JsonProperty("representativeId")
    private Long representativeId;
    @JsonProperty("startDate")
    private String startDate;
    @JsonProperty("endDate")
    private String endDate;
    @JsonProperty("status")
    private String status;
    @JsonProperty("ftxRegistration")
    private String ftxRegistration;
    @JsonProperty("sigPath")
    private String sigPath;
    @JsonProperty("mrpaXmlPath")
    private String mrpaXmlPath;
    @JsonProperty("recalledXmlPath")
    private String recalledXmlPath;
    @JsonProperty("userId")
    private Long userId;
    @JsonProperty("principalFio")
    private String principalFio;
    @JsonProperty("representativeFio")
    private String representativeFio;

    public MrpaRegistryWithFioDTO(Long id, Long userId, String guid, String internalNumber, String startDate, String endDate, @NotNull StatusTypeEnum status,
                                  @NotNull FtxRegistrationTypeEnum ftxRegistration, String sigPath, String mrpaXmlPath, String recalledXmlPath, Long organizationId,
                                  Long principalId, Long representativeId, String principalSurname, String principalName, String principalPatronymic,
                                  String representativeSurname, String representativeName, String representativePatronymic) {
        super();
        this.id = id;
        this.guid = guid;
        this.internalNumber = internalNumber;
        this.organizationId = organizationId;
        this.principalId = principalId;
        this.representativeId = representativeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status.getTitle();
        this.ftxRegistration = ftxRegistration.getTitle();
        this.sigPath = sigPath;
        this.mrpaXmlPath = mrpaXmlPath;
        this.recalledXmlPath = recalledXmlPath;
        this.userId = userId;
        this.principalFio = principalSurname + ' ' + principalName + ' ' + principalPatronymic;
        this.representativeFio = representativeSurname + ' ' + representativeName + ' ' + representativePatronymic;
    }
}
