package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import su.opencode.digsigcik.m4d.enums.AuthorTypeEnum;
import su.opencode.digsigcik.m4d.enums.FileLogicalTypeEnum;
import su.opencode.digsigcik.m4d.enums.FileMimeTypeEnum;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

    public OrganizationDto organization;

    @Data
    public static class OrganizationDto {
        private Long id;
        private String inn;
        private String kpp;
        private String ogrn;
        private String legalAddress;
        private String regionCode;
        private String name;

    }

    public PrincipalDto principal;

    @Data
    public static class PrincipalDto {
        private Long id;
        private String inn;
        private String snils;
    }

    public PrincipalDetailDto principalDetail;

    @Data
    public static class PrincipalDetailDto {
        private Long id;
        private String jobTitle;
        private String surname;
        private String patronymic;
        private String name;
    }

    public RepresentativeDto representative;

    @Data
    public static class RepresentativeDto {
        private Long id;
        private String inn;
        private String snils;
        private String birthDate;
        private String surname;
        private String patronymic;
        private String name;

        public RepresentativePassportDto representativePassport;
    }

    @Data
    public static  class RepresentativePassportDto {
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
    private SigFileDto sigFile;

    @Data
    public static class SigFileDto {
        @JsonProperty("id")
        private Long id;
        @JsonProperty("fileLogicalType")
        @Enumerated(EnumType.STRING)
        private FileLogicalTypeEnum fileLogicalType;
        @JsonProperty("fileMimeType")
        @Enumerated(EnumType.STRING)
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
    public RecallDto recall;

    @Data
    public static class RecallDto {
        @JsonProperty("id")
        private Long id;
        @JsonProperty("author")
        private String author;
        @JsonProperty("argument")
        private String argument;
        @JsonProperty("signature")
        private SigFileDto sigFile;

        @Data
        public static class SigFileDto {
            @JsonProperty("id")
            private Long id;
            @JsonProperty("fileLogicalType")
            @Enumerated(EnumType.STRING)
            private FileLogicalTypeEnum fileLogicalType;
            @JsonProperty("fileMimeType")
            @Enumerated(EnumType.STRING)
            private FileMimeTypeEnum fileMimeType;
            @JsonProperty("filename")
            private String filename;
            @JsonProperty("content")
            private String content;
        }
    }

    public List<CredentialDto> credentials;

    @Data
    public static class CredentialDto {
        @JsonProperty("id")
        private Long id;
        @JsonProperty("code")
        private String code;
        @JsonProperty("name")
        private String name;
    }

    public M4dResponseDto(Long id, String guid, String internalNumber, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime createDate,
                          Long principal_id, String principal_inn, String principal_snils, Long principal_detail_id, String principal_detail_jobTitle,
                          String principal_detail_name, String principal_detail_surname, String principal_detail_patronymic,
                          Long representative_id, String representative_inn, String representative_snils, LocalDateTime representative_birthDate,
                          String representative_name, String representative_surname, String representative_patronymic) {
        this.id = id;
        this.guid = guid;
        this.internalNumber = internalNumber;
        this.startDate = startDate.toString();
        this.endDate = endDate.toString();
        this.createDate = createDate.toString();

        PrincipalDto principalDTO = new PrincipalDto();
        principalDTO.id = principal_id;
        principalDTO.inn = principal_inn;
        principalDTO.snils = principal_snils;
        this.principal = principalDTO;

        PrincipalDetailDto principalDetailDto = new PrincipalDetailDto();
        principalDetailDto.id = principal_detail_id;
        principalDetailDto.jobTitle = principal_detail_jobTitle;
        principalDetailDto.name = principal_detail_name;
        principalDetailDto.surname = principal_detail_surname;
        principalDetailDto.patronymic = principal_detail_patronymic;
        this.principalDetail = principalDetailDto;

        RepresentativeDto representativeDTO = new RepresentativeDto();
        representativeDTO.id = representative_id;
        representativeDTO.inn = representative_inn;
        representativeDTO.snils = representative_snils;
        representativeDTO.birthDate = representative_birthDate.toString();
        representativeDTO.name = representative_name;
        representativeDTO.surname = representative_surname;
        representativeDTO.patronymic = representative_patronymic;
        this.representative = representativeDTO;
    }

    public M4dResponseDto(Long id, String guid, String internalNumber,
                          Long principalId, String principalInn, String principalSnils, Long principalDetailId, String principalDetailJobTitle,
                          String principalDetailName, String principalDetailSurname, String principalDetailPatronymic,
                          Long representativeId, String representativeInn, String representativeSnils,
                          LocalDateTime representative_birthDate, String representativeName, String representativeSurname,
                          String representativePatronymic,
                          LocalDateTime startDate, LocalDateTime endDate, LocalDateTime createDate,
                          Long recallId, AuthorTypeEnum recallAuthor, String recallArgument) {

        this.id = id;
        this.guid = guid;
        this.internalNumber = internalNumber;
        this.startDate = startDate.toString();
        this.endDate = endDate.toString();
        this.createDate = createDate.toString();


        this.principal = new PrincipalDto();
        this.principal.setId(principalId);
        this.principal.setInn(principalInn);
        this.principal.setSnils(principalSnils);

        this.principalDetail = new PrincipalDetailDto();
        this.principalDetail.setId(principalDetailId);
        this.principalDetail.setJobTitle(principalDetailJobTitle);
        this.principalDetail.setName(principalDetailName);
        this.principalDetail.setSurname(principalDetailSurname);
        this.principalDetail.setPatronymic(principalDetailPatronymic);

        this.representative = new RepresentativeDto();
        this.representative.setId(representativeId);
        this.representative.setInn(representativeInn);
        this.representative.setSnils(representativeSnils);
        this.representative.setBirthDate(representative_birthDate.toString());
        this.representative.setName(representativeName);
        this.representative.setSurname(representativeSurname);
        this.representative.setPatronymic(representativePatronymic);

        this.recall = new RecallDto();
        this.recall.setId(recallId);
        this.recall.setAuthor(recallAuthor.toString());
        this.recall.setArgument(recallArgument);
    }
}
