package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganisationDTO {
    @JsonProperty("name")
    private String name;
    @JsonProperty("inn")
    private Integer inn;
    @JsonProperty("kpp")
    private Integer kpp;
    @JsonProperty("ogrn")
    private Integer ogrn;
    @JsonProperty("legalAddress")
    private String legalAddress;
    @JsonProperty("regionCode")
    private Long regionCode;
}
