package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PrincipalDTO {
    @JsonProperty("name")
    private String name;
    @JsonProperty("surname")
    private String surname;
    @JsonProperty("patronymic")
    private String patronymic;
    @JsonProperty("inn")
    private String inn;
    @JsonProperty("snils")
    private String snils;
    @JsonProperty("jobTitle")
    private String jobTitle;
}
