package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ReportCheckStatusM4dDto {
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
