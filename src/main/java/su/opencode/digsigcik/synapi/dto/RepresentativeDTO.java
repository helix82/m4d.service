package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

@Getter
@Setter
public class RepresentativeDTO {
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
    @JsonProperty("birthDate")
    private DateTime birthDate;
}
