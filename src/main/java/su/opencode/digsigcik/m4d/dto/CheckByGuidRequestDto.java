package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CheckByGuidRequestDto {
    @JsonProperty("guid")
    private String guid;
    @JsonProperty("principalInn")
    private String principalInn;
    @JsonProperty("representativeInn")
    private String representativeInn;

    @Override
    public String toString() {
        return "{guid='" + guid + "', principalInn='" + principalInn + "', representativeInn='" + representativeInn + "'}";
    }
}
