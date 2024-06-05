package su.opencode.digsigcik.synapi.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import su.opencode.digsigcik.synapi.enums.FtxRegistrationTypeEnum;
import su.opencode.digsigcik.synapi.enums.StatusTypeEnum;

@Data
public class MrpaRegistryFullDTO {

    private Long id;
    private Long userId;
    private String guid;
    private String internalNumber;
    private OrganizationDTO organization;

    @Data
    private static class OrganizationDTO {
        private Long id;
        private String inn;
        private String kpp;
        private String ogrn;
        private String legalAddress;
        private String regionCode;
        private String name;

    }

    private PrincipalDTO principal;

    @Data
    private static class PrincipalDTO {
        private Long id;
        private String inn;
        private String snils;
        private String jobTitle;
        private String surname;
        private String patronymic;
        private String name;
    }

    private RepresentativeDTO representative;

    @Data
    private static class RepresentativeDTO {
        private Long id;
        private String inn;
        private String snils;
        private String birthDate;
        private String surname;
        private String patronymic;
        private String name;

        private RepresentativePassportDTO representativePassport;
    }

    @Data
    private static  class RepresentativePassportDTO {
        private Long id;
        private String seriesNumber;
        private String dateIssue;
        private String whoIssue;
        private String departmentCode;
    }

    private String startDate;
    private String endDate;
    private String status;
    private String ftxRegistration;
    private String sigPath;
    private String mrpaXmlPath;
    private String recalledXmlPath;

    private CredentialDTO credential;

    @Data
    private static class CredentialDTO {
        private Long id;
        private String code;
        private String name;
    }

    public MrpaRegistryFullDTO(Long id, Long userId, String guid, String internalNumber, String startDate, String endDate, @NotNull StatusTypeEnum status,
                               @NotNull FtxRegistrationTypeEnum ftxRegistration, String sigPath, String mrpaXmlPath, String recalledXmlPath,
                               Long principal_id, String principal_inn, String principal_snils, String principal_jobTitle, String principal_name, String principal_surname, String principal_patronymic,
                               Long representative_id, String representative_inn, String representative_snils, String representative_birthDate, String representative_name, String representative_surname, String representative_patronymic,
                               Long representativePassport_id, String representativePassport_dateIssue, String representativePassport_whoIssue, String representativePassport_seriesNumber, String representativePassport_departmentCode,
                               Long organisation_id, String organisation_inn, String organisation_kpp, String organisation_ogrn, String organisation_name, String organisation_regionCode, String organisation_legalAddress,
                               Long credential_id, String credential_code, String credential_name) {
        super();
        this.id = id;
        this.userId = userId;
        this.guid = guid;
        this.internalNumber = internalNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status.getTitle();
        this.ftxRegistration = ftxRegistration.getTitle();
        this.sigPath = sigPath;
        this.mrpaXmlPath = mrpaXmlPath;
        this.recalledXmlPath = recalledXmlPath;

        MrpaRegistryFullDTO.PrincipalDTO principalDTO = new PrincipalDTO();
        principalDTO.id = principal_id;
        principalDTO.inn = principal_inn;
        principalDTO.snils = principal_snils;
        principalDTO.jobTitle = principal_jobTitle;
        principalDTO.name = principal_name;
        principalDTO.surname = principal_surname;
        principalDTO.patronymic = principal_patronymic;
        this.principal = principalDTO;

        MrpaRegistryFullDTO.RepresentativeDTO representativeDTO = new RepresentativeDTO();
        representativeDTO.id = representative_id;
        representativeDTO.inn = representative_inn;
        representativeDTO.snils = representative_snils;
        representativeDTO.birthDate = representative_birthDate;
        representativeDTO.name = representative_name;
        representativeDTO.surname = representative_surname;
        representativeDTO.patronymic = representative_patronymic;
        this.representative = representativeDTO;

        MrpaRegistryFullDTO.RepresentativePassportDTO representativePassportDTO = new RepresentativePassportDTO();
        representativePassportDTO.id = representativePassport_id;
        representativePassportDTO.seriesNumber = representativePassport_seriesNumber;
        representativePassportDTO.dateIssue = representativePassport_dateIssue;
        representativePassportDTO.whoIssue = representativePassport_whoIssue;
        representativePassportDTO.departmentCode = representativePassport_departmentCode;
        this.representative.representativePassport = representativePassportDTO;

        MrpaRegistryFullDTO.OrganizationDTO organizationDTO = new OrganizationDTO();
        organizationDTO.id = organisation_id;
        organizationDTO.inn = organisation_inn;
        organizationDTO.kpp = organisation_kpp;
        organizationDTO.ogrn = organisation_ogrn;
        organizationDTO.name = organisation_name;
        organizationDTO.regionCode = organisation_regionCode;
        organizationDTO.legalAddress = organisation_legalAddress;
        this.organization = organizationDTO;

        MrpaRegistryFullDTO.CredentialDTO credentialDTO = new CredentialDTO();
        credentialDTO.id = credential_id;
        credentialDTO.code = credential_code;
        credentialDTO.name = credential_name;
        this.credential = credentialDTO;
    }

}
