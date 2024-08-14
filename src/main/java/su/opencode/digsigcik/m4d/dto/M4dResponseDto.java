package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class M4dResponseDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("guid")
    private String guid;

    @JsonProperty("internalNumber")
    private String internalNumber;

    public OrganizationDTO organization;

    @Data
    public static class OrganizationDTO {
        private Long id;
        private String inn;
        private String kpp;
        private String ogrn;
        private String legalAddress;
        private String regionCode;
        private String name;

    }

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

        public RepresentativePassportDTO representativePassport;
    }

    @Data
    public static  class RepresentativePassportDTO {
        private Long id;
        private String seriesNumber;
        private String dateIssue;
        private String whoIssue;
        private String departmentCode;
    }

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

    @JsonProperty("recalledXmlPath")
    private String recalledXmlPath;

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

    public static M4dResponseDto fill(String id, String guid, String internalNumber, String startDate, String endDate, String status, String createDate,
                                      String principal_id, String principal_inn, String principal_snils, String principal_jobTitle, String principal_name, String principal_surname, String principal_patronymic,
                                      String representative_id, String representative_inn, String representative_snils, String representative_birthDate, String representative_name, String representative_surname, String representative_patronymic) {

        M4dResponseDto m4dFullDto = new M4dResponseDto();
        m4dFullDto.setId(Long.valueOf(id));
        m4dFullDto.setGuid(guid);
        m4dFullDto.setInternalNumber(internalNumber);
        m4dFullDto.setStartDate(startDate);
        m4dFullDto.setEndDate(endDate);
        m4dFullDto.setStatus(status);
        m4dFullDto.setCreateDate(createDate);

        M4dResponseDto.PrincipalDTO principalDTO = new PrincipalDTO();
        principalDTO.id = Long.valueOf(principal_id);
        principalDTO.inn = principal_inn;
        principalDTO.snils = principal_snils;
        principalDTO.jobTitle = principal_jobTitle;
        principalDTO.name = principal_name;
        principalDTO.surname = principal_surname;
        principalDTO.patronymic = principal_patronymic;
        m4dFullDto.setPrincipal(principalDTO);

        M4dResponseDto.RepresentativeDTO representativeDTO = new RepresentativeDTO();
        representativeDTO.id = Long.valueOf(representative_id);
        representativeDTO.inn = representative_inn;
        representativeDTO.snils = representative_snils;
        representativeDTO.birthDate = representative_birthDate;
        representativeDTO.name = representative_name;
        representativeDTO.surname = representative_surname;
        representativeDTO.patronymic = representative_patronymic;
        m4dFullDto.setRepresentative(representativeDTO);

        return m4dFullDto;
    }

    public static M4dResponseDto fill(String id, String guid, String internalNumber, String startDate, String endDate, String status, String sigPath, String createDate, String recalledXmlPath,
                                      String principal_id, String principal_inn, String principal_snils, String principal_jobTitle, String principal_name, String principal_surname, String principal_patronymic,
                                      String representative_id, String representative_inn, String representative_snils, String representative_birthDate, String representative_name, String representative_surname, String representative_patronymic,
                                      String representativePassport_id, String representativePassport_dateIssue, String representativePassport_whoIssue, String representativePassport_seriesNumber, String representativePassport_departmentCode,
                                      String organisation_id, String organisation_inn, String organisation_kpp, String organisation_ogrn, String organisation_name, String organisation_regionCode, String organisation_legalAddress) {

        M4dResponseDto m4dFullDto = new M4dResponseDto();
        m4dFullDto.setId(Long.valueOf(id));
        m4dFullDto.setGuid(guid);
        m4dFullDto.setInternalNumber(internalNumber);
        m4dFullDto.setStartDate(startDate);
        m4dFullDto.setEndDate(endDate);
        m4dFullDto.setStatus(status);
        m4dFullDto.setCreateDate(createDate);
        m4dFullDto.setSigPath(sigPath);
        m4dFullDto.setRecalledXmlPath(recalledXmlPath);

        M4dResponseDto.PrincipalDTO principalDTO = new PrincipalDTO();
        principalDTO.id = Long.valueOf(principal_id);
        principalDTO.inn = principal_inn;
        principalDTO.snils = principal_snils;
        principalDTO.jobTitle = principal_jobTitle;
        principalDTO.name = principal_name;
        principalDTO.surname = principal_surname;
        principalDTO.patronymic = principal_patronymic;
        m4dFullDto.setPrincipal(principalDTO);

        M4dResponseDto.RepresentativeDTO representativeDTO = new RepresentativeDTO();
        representativeDTO.id = Long.valueOf(representative_id);
        representativeDTO.inn = representative_inn;
        representativeDTO.snils = representative_snils;
        representativeDTO.birthDate = representative_birthDate;
        representativeDTO.name = representative_name;
        representativeDTO.surname = representative_surname;
        representativeDTO.patronymic = representative_patronymic;
        m4dFullDto.setRepresentative(representativeDTO);

        M4dResponseDto.RepresentativePassportDTO representativePassportDTO = new RepresentativePassportDTO();
        representativePassportDTO.id = Long.valueOf(representativePassport_id);
        representativePassportDTO.seriesNumber = representativePassport_seriesNumber;
        representativePassportDTO.dateIssue = representativePassport_dateIssue;
        representativePassportDTO.whoIssue = representativePassport_whoIssue;
        representativePassportDTO.departmentCode = representativePassport_departmentCode;
        m4dFullDto.representative.setRepresentativePassport(representativePassportDTO);

        M4dResponseDto.OrganizationDTO organizationDTO = new OrganizationDTO();
        organizationDTO.id = Long.valueOf(organisation_id);
        organizationDTO.inn = organisation_inn;
        organizationDTO.kpp = organisation_kpp;
        organizationDTO.ogrn = organisation_ogrn;
        organizationDTO.name = organisation_name;
        organizationDTO.regionCode = organisation_regionCode;
        organizationDTO.legalAddress = organisation_legalAddress;
        m4dFullDto.setOrganization(organizationDTO);

        return m4dFullDto;
    }

}
