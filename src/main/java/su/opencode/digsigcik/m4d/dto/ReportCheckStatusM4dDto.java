package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportCheckStatusM4dDto {
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
    @JsonProperty("statusDate")
    private LocalDateTime statusDate;
    @JsonProperty("innRepresentative")
    private String innRepresentative;
    @JsonProperty("fioRepresentative")
    private String fioRepresentative;
    @JsonProperty("innPrincipal")
    private String innPrincipal;
    @JsonProperty("fioPrincipal")
    private String fioPrincipal;
    @JsonProperty("innOrganisation")
    private String innOrganisation;
    @JsonProperty("nameOrganisation")
    private String nameOrganisation;
}
