package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrincipalDTO {
    @JsonProperty("name")
    private String name;
    @JsonProperty("surname")
    private String surname;
    @JsonProperty("patronymic")
    private String patronymic;
    @JsonProperty("inn")
    private Integer inn;
    @JsonProperty("snils")
    private Integer snils;
    @JsonProperty("jobTitle")
    private String jobTitle;
}
