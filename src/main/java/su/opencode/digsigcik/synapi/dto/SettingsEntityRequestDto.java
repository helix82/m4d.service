package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SettingsEntityRequestDto {
    @JsonProperty("key")
    private String key;
    @JsonProperty("value")
    private String value;
}
