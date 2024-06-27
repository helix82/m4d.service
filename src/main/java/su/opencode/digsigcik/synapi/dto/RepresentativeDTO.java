package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RepresentativeDTO {
    @JsonProperty("id")
    private Long id;

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

    @JsonProperty("birthDate")
    private String birthDate;

    @JsonProperty("representativePassports")
    private List<RepresentativePassportDTO> representativePassports;
}
