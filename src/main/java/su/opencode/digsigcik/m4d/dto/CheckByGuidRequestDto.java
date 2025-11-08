package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import su.opencode.digsigcik.audit.AllowAudit;

@Data
public class CheckByGuidRequestDto {
    @JsonProperty("guid")
    @AllowAudit
    private String guid;
    @JsonProperty("principalInn")
    @AllowAudit
    private String principalInn;
    @JsonProperty("representativeInn")
    @AllowAudit
    private String representativeInn;

    @Override
    public String toString() {
        return "{guid='" + guid + "', principalInn='" + principalInn + "', representativeInn='" + representativeInn + "'}";
    }
}
