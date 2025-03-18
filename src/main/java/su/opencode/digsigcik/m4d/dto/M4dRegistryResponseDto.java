package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class M4dRegistryResponseDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("guid")
    private String guid;

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
        private String regionName;
        private String locality;
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
    }

    public RepresentativeDetailDto representativeDetail;

    @Data
    public static  class RepresentativeDetailDto {
        private Long id;
        private String surname;
        private String patronymic;
        private String name;
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

    @JsonProperty("errorDetail")
    private String errorDetail;

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

    @JsonProperty("xmlFile")
    private XmlFileDto xmlFile;

    @Data
    public static class XmlFileDto {
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

    @JsonProperty("createDate")
    private String createDate;

    @JsonProperty("recall")
    public RecallDto recall;

    @Data
    public static class RecallDto {
        @JsonProperty("id")
        private Long id;
        @JsonProperty("argument")
        private String argument;
        @JsonProperty("status")
        private String status;
        @JsonProperty("errorDetail")
        private String errorDetail;
        @JsonProperty("createDate")
        private String createDate;

        @JsonProperty("signature")
        private SigFileRecallDto sigFile;

        @Data
        public static class SigFileRecallDto {
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

        @JsonProperty("xmlFile")
        private XmlFileRecallDto xmlFile;

        @Data
        public static class XmlFileRecallDto {
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

    public M4dRegistryResponseDto(Long id, String guid, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime createDate,
                                  Long principal_id, String principal_inn, String principal_snils, Long principal_detail_id, String principal_detail_jobTitle,
                                  String principal_detail_name, String principal_detail_surname, String principal_detail_patronymic,
                                  Long representative_id, String representative_inn, String representative_snils, LocalDateTime representative_birthDate,
                                  String representative_name, String representative_surname, String representative_patronymic) {
        this.id = id;
        this.guid = guid;
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
        this.representative = representativeDTO;

        RepresentativeDetailDto representativeDetailDto = new RepresentativeDetailDto();
        representativeDetailDto.name = representative_name;
        representativeDetailDto.surname = representative_surname;
        representativeDetailDto.patronymic = representative_patronymic;
        this.representativeDetail = representativeDetailDto;
    }

    public M4dRegistryResponseDto(Long id, String guid,
                                  Long principalId, String principalInn, String principalSnils, Long principalDetailId, String principalDetailJobTitle,
                                  String principalDetailName, String principalDetailSurname, String principalDetailPatronymic,
                                  Long representativeId, String representativeInn, String representativeSnils,
                                  LocalDateTime representative_birthDate, String representativeName, String representativeSurname,
                                  String representativePatronymic, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime createDate, Long recallId) {

        this.id = id;
        this.guid = guid;
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

        this.representativeDetail = new RepresentativeDetailDto();
        this.representativeDetail.setName(representativeName);
        this.representativeDetail.setSurname(representativeSurname);
        this.representativeDetail.setPatronymic(representativePatronymic);

        this.recall = new RecallDto();
        this.recall.setId(recallId);
    }
}
