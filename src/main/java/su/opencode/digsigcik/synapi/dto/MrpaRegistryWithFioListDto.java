package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import su.opencode.digsigcik.synapi.enums.StatusTypeEnum;

import java.util.List;

@Data
public class MrpaRegistryWithFioListDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("guid")
    private String guid;

    @JsonProperty("internalNumber")
    private String internalNumber;

    @JsonProperty("organizationId")
    private Long organizationId;

    public PrincipalDTO principal;

    @Data
    public static class PrincipalDTO {
        private Long id;
        private String inn;
        private String snils;
        private String jobTitle;
        private String surname;
        private String patronymic;
        private String name;
    }

    public RepresentativeDTO representative;

    @Data
    public static class RepresentativeDTO {
        private Long id;
        private String inn;
        private String snils;
        private String birthDate;
        private String surname;
        private String patronymic;
        private String name;
    }


    @JsonProperty("startDate")
    private String startDate;

    @JsonProperty("endDate")
    private String endDate;

    @JsonProperty("status")
    private String status;

    @JsonProperty("mrpaXmlFilename")
    private String mrpaXmlFilename;

    @JsonProperty("principalFio")
    private String principalFio;

    @JsonProperty("representativeFio")
    private String representativeFio;

    public List<MrpaRegistryWithFioDTO.CredentialDTO> credentials;

    @Data
    public static class CredentialDTO {
        @JsonProperty("id")
        private Long id;
        @JsonProperty("code")
        private String code;
        @JsonProperty("name")
        private String name;
    }

    public MrpaRegistryWithFioListDto(Long id, String guid, String internalNumber, String startDate, String endDate, @NotNull StatusTypeEnum status, String mrpaXmlFilename, Long organizationId,
                                      Long principal_id, String principal_inn, String principal_snils, String principal_jobTitle, String principal_name, String principal_surname, String principal_patronymic,
                                      Long representative_id, String representative_inn, String representative_snils, String representative_birthDate, String representative_name, String representative_surname, String representative_patronymic) {
        super();
        this.id = id;
        this.guid = guid;
        this.internalNumber = internalNumber;
        this.organizationId = organizationId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status.toString();
        this.mrpaXmlFilename = mrpaXmlFilename;
        this.principalFio = principal_surname + ' ' + principal_name + ' ' + principal_patronymic;
        this.representativeFio = representative_surname + ' ' + representative_name + ' ' + representative_patronymic;

        MrpaRegistryWithFioListDto.PrincipalDTO principalDTO = new MrpaRegistryWithFioListDto.PrincipalDTO();
        principalDTO.id = principal_id;
        principalDTO.inn = principal_inn;
        principalDTO.snils = principal_snils;
        principalDTO.jobTitle = principal_jobTitle;
        principalDTO.name = principal_name;
        principalDTO.surname = principal_surname;
        principalDTO.patronymic = principal_patronymic;
        this.principal = principalDTO;

        MrpaRegistryWithFioListDto.RepresentativeDTO representativeDTO = new MrpaRegistryWithFioListDto.RepresentativeDTO();
        representativeDTO.id = representative_id;
        representativeDTO.inn = representative_inn;
        representativeDTO.snils = representative_snils;
        representativeDTO.birthDate = representative_birthDate;
        representativeDTO.name = representative_name;
        representativeDTO.surname = representative_surname;
        representativeDTO.patronymic = representative_patronymic;
        this.representative = representativeDTO;
    }
}
