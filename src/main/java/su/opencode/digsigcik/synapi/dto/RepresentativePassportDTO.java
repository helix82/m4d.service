package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepresentativePassportDTO {
    @JsonProperty("seriesNumber")
    private String seriesNumber;
    @JsonProperty("dateIssue")
    private String dateIssue;
    @JsonProperty("whoIssue")
    private String whoIssue;
    @JsonProperty("departmentCode")
    private String departmentCode;
}
