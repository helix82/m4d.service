package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import su.opencode.digsigcik.synapi.enums.StatusTypeEnum;

import java.util.List;

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

    @JsonProperty("sigPath")
    private String sigPath;

    @JsonProperty("mrpaXml")
    private String mrpaXml;

    @JsonProperty("createDate")
    private String createDate;

    @JsonProperty("mrpaXmlFilename")
    private String mrpaXmlFilename;

    @JsonProperty("recalledXmlPath")
    private String recalledXmlPath;

    @JsonProperty("principalFio")
    private String principalFio;

    @JsonProperty("representativeFio")
    private String representativeFio;

    public List<CredentialDTO> credentials;

    @Data
    public static class CredentialDTO {
        @JsonProperty("id")
        private Long id;
        @JsonProperty("code")
        private String code;
        @JsonProperty("name")
        private String name;
    }

    public MrpaRegistryWithFioDTO(Long id, String guid, String internalNumber, String startDate, String endDate, @NotNull StatusTypeEnum status,
                                  String sigPath, String createDate, String mrpaXmlFilename, String recalledXmlPath, Long organizationId,
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
        this.status = status.toString();
        this.sigPath = sigPath;
        this.createDate = createDate;
        this.mrpaXmlFilename = mrpaXmlFilename;
        this.recalledXmlPath = recalledXmlPath;
        this.principalFio = principalSurname + ' ' + principalName + ' ' + principalPatronymic;
        this.representativeFio = representativeSurname + ' ' + representativeName + ' ' + representativePatronymic;
    }

}
