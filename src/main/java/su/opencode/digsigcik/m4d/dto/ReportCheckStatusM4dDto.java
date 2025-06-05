package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import su.opencode.digsigcik.audit.AllowAudit;

import java.util.List;

@Data
public class ReportCheckStatusM4dDto {
    @JsonProperty("source")
    @AllowAudit
    private String source;
    @JsonProperty("report")
    @AllowAudit
    public Report report;

    @Data
    public static class Report {
        @JsonProperty("guid")
        @AllowAudit
        private String guid;

        @JsonProperty("startDate")
        @AllowAudit
        private String startDate;

        @JsonProperty("endDate")
        @AllowAudit
        private String endDate;

        @JsonProperty("status")
        @AllowAudit
        private String status;

        @JsonProperty("statusInRr")
        @AllowAudit
        private String statusInRr;

        @JsonProperty("errorInRrDetail")
        @AllowAudit
        private String errorInRrDetail;

        @JsonProperty("representative")
        @AllowAudit
        public List<Representative> representative;

        @Data
        public static class Representative {
            @JsonProperty("inn")
            @AllowAudit
            public String inn;

            @JsonProperty("fio")
            @AllowAudit
            private String fio;
        }

        @JsonProperty("principal")
        @AllowAudit
        public List<Principal> principal;

        @Data
        public static class Principal {
            @JsonProperty("inn")
            @AllowAudit
            private String inn;

            @JsonProperty("fio")
            @AllowAudit
            private String fio;
        }

        @JsonProperty("innOrganisation")
        @AllowAudit
        private String innOrganisation;

        @JsonProperty("nameOrganisation")
        @AllowAudit
        private String nameOrganisation;

        @JsonProperty("credentials")
        @AllowAudit
        public List<Credential> credentials;

        @Data
        public static class Credential {
            @JsonProperty("code")
            @AllowAudit
            private String code;

            @JsonProperty("name")
            @AllowAudit
            private String name;
        }

        @JsonProperty("sigFile")
        @AllowAudit
        private SigFileDto sigFile;

        @Data
        public static class SigFileDto {
            @JsonProperty("id")
            @AllowAudit
            private Long id;

            @JsonProperty("fileLogicalType")
            @AllowAudit
            private String fileLogicalType;

            @JsonProperty("fileMimeType")
            @AllowAudit
            private String fileMimeType;

            @JsonProperty("filename")
            @AllowAudit
            private String filename;

            @JsonProperty("content")
            private String content;
        }
    }

}
