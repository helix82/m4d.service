package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import su.opencode.digsigcik.m4d.enums.FileLogicalTypeEnum;
import su.opencode.digsigcik.m4d.enums.FileMimeTypeEnum;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

    @JsonProperty("sigFile")
    private SigFileDTO sigFile;

    @Data
    public static class SigFileDTO {
        @JsonProperty("id")
        private Long id;
        @JsonProperty("fileLogicalType")
        private FileLogicalTypeEnum fileLogicalType;
        @JsonProperty("fileMimeType")
        private FileMimeTypeEnum fileMimeType;
        @JsonProperty("filename")
        private String filename;
        @JsonProperty("content")
        private String content;
    }

    @JsonProperty("mrpaXml")
    private String mrpaXml;

    @JsonProperty("createDate")
    private String createDate;

    @JsonProperty("recallXml")
    private String recallXml;

    @JsonProperty("recall")
    public RecallDTO recall;

    @Data
    public static class RecallDTO {
        @JsonProperty("id")
        private Long id;
        @JsonProperty("author")
        private String author;
        @JsonProperty("argument")
        private String argument;
        @JsonProperty("signature")
        private SigFileDTO sigFile;

        @Data
        public static class SigFileDTO {
            @JsonProperty("id")
            private Long id;
            @JsonProperty("fileLogicalType")
            private FileLogicalTypeEnum fileLogicalType;
            @JsonProperty("fileMimeType")
            private FileMimeTypeEnum fileMimeType;
            @JsonProperty("filename")
            private String filename;
            @JsonProperty("content")
            private String content;
        }
    }

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

    public M4dResponseDto(Long id, String guid, String internalNumber, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime createDate,
                          Long principal_id, String principal_inn, String principal_snils, String principal_jobTitle,
                          String principal_name, String principal_surname, String principal_patronymic,
                          Long representative_id, String representative_inn, String representative_snils, LocalDateTime representative_birthDate,
                          String representative_name, String representative_surname, String representative_patronymic) {
        this.id = id;
        this.guid = guid;
        this.internalNumber = internalNumber;
        this.startDate = startDate.toString();
        this.endDate = endDate.toString();
        this.createDate = createDate.toString();

        PrincipalDTO principalDTO = new PrincipalDTO();
        principalDTO.id = principal_id;
        principalDTO.inn = principal_inn;
        principalDTO.snils = principal_snils;
        principalDTO.jobTitle = principal_jobTitle;
        principalDTO.name = principal_name;
        principalDTO.surname = principal_surname;
        principalDTO.patronymic = principal_patronymic;
        this.principal = principalDTO;

        RepresentativeDTO representativeDTO = new RepresentativeDTO();
        representativeDTO.id = representative_id;
        representativeDTO.inn = representative_inn;
        representativeDTO.snils = representative_snils;
        representativeDTO.birthDate = representative_birthDate.toString();
        representativeDTO.name = representative_name;
        representativeDTO.surname = representative_surname;
        representativeDTO.patronymic = representative_patronymic;
        this.representative = representativeDTO;
    }

}
