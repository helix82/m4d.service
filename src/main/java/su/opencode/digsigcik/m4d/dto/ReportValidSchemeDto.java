package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ReportValidSchemeDto {
    @JsonProperty("fileName")
    private String fileName;
    @JsonProperty("reqSchema")
    private String reqSchema;
    @JsonProperty("outSchema")
    private String outSchema;
    @JsonProperty("result")
    private String result;
    @JsonProperty("errorDetail")
    private String errorDetail;
}
