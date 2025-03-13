package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ReportCheckStatusM4dDto {
    @JsonProperty("source")
    private String source;
    @JsonProperty("report")
    public Report report;

    @Data
    public static class Report {
        @JsonProperty("guid")
        private String guid;
        @JsonProperty("startDate")
        private String startDate;
        @JsonProperty("endDate")
        private String endDate;
        @JsonProperty("status")
        private String status;
        @JsonProperty("errorDetail")
        private String errorDetail;
        @JsonProperty("representative")
        public List<Representative> representative;

        @Data
        public static class Representative {
            @JsonProperty("inn")
            public String inn;
            @JsonProperty("fio")
            private String fio;
        }

        @JsonProperty("principal")
        public List<Principal> principal;

        @Data
        public static class Principal {
            @JsonProperty("inn")
            private String inn;
            @JsonProperty("fio")
            private String fio;
        }

        @JsonProperty("innOrganisation")
        private String innOrganisation;
        @JsonProperty("nameOrganisation")
        private String nameOrganisation;
        @JsonProperty("credentials")
        public List<Credential> credentials;

        @Data
        public static class Credential {
            @JsonProperty("code")
            private String code;
            @JsonProperty("name")
            private String name;
        }
    }

}
