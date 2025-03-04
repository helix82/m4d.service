package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class M4dRequestDto {
    public MrpaRegistry mrpaRegistry;

    @Data
    public static class MrpaRegistry{
        private Long id;

        @JsonProperty("guid")
        private String guid;

        @JsonProperty("internalNumber")
        private String internalNumber;

        @JsonProperty("startDate")
        private String startDate;

        @JsonProperty("endDate")
        private String endDate;
    }

    public Organisation organisation;

    @Data
    public static class Organisation {
        private Long id;
        private String inn;
        private String kpp;
        private String ogrn;
        private String legalAddress;
        private String regionCode;
        private String name;

    }

    public Principal principal;

    @Data
    public static class Principal {
        private Long id;
        private String inn;
        private String snils;
    }

    public PrincipalDetail principalDetail;

    @Data
    public static class PrincipalDetail {
        private Long id;
        private String jobTitle;
        private String surname;
        private String patronymic;
        private String name;
        private Long principalId;
    }

    public Representative representative;

    @Data
    public static class Representative {
        private Long id;
        private String inn;
        private String snils;
        private String birthDate;
    }

    public RepresentativeDetail representativeDetail;

    @Data
    public static  class RepresentativeDetail {
        private Long id;
        private String surname;
        private String patronymic;
        private String name;
        private String seriesNumber;
        private String dateIssue;
        private String whoIssue;
        private String departmentCode;
        private Long representativeId;
    }

    @JsonProperty("credentials")
    private Long[] credentials;

    @JsonProperty("createDate")
    private String createDate;

    public void setGuid() {
        this.mrpaRegistry.guid = UUID.randomUUID()
                .toString();
    }
}
