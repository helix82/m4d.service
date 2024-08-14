package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class M4dRequestSaveFullDto {
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
        private String inn;
        private String snils;
        private String jobTitle;
        private String surname;
        private String patronymic;
        private String name;
    }

    public Representative representative;

    @Data
    public static class Representative {
        private String inn;
        private String snils;
        private String birthDate;
        private String surname;
        private String patronymic;
        private String name;

        public RepresentativePassport representativePassport;
    }

    @Data
    public static  class RepresentativePassport {
        private String seriesNumber;
        private String dateIssue;
        private String whoIssue;
        private String departmentCode;
    }

    @JsonProperty("credentials")
    private Long[] credentials;
}
