package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrganizationDto {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("inn")
    private String inn;
    @JsonProperty("kpp")
    private String kpp;
    @JsonProperty("ogrn")
    private String ogrn;
    @JsonProperty("legalAddress")
    private String legalAddress;
    @JsonProperty("regionCode")
    private String regionCode;
    @JsonProperty("regionName")
    private String regionName;
    @JsonProperty("locality")
    private String locality;
}
