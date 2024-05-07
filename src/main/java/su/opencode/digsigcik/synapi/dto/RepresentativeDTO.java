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
    @JsonProperty("inn")
    private Integer inn;
    @JsonProperty("kpp")
    private Integer kpp;
    @JsonProperty("snils")
    private Integer snils;
    @JsonProperty("birthDate")
    private DateTime birthDate;
}
