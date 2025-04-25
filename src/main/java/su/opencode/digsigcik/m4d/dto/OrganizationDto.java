package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import su.opencode.digsigcik.audit.AllowAudit;

@Data
public class OrganizationDto {
    @JsonProperty("id")
    @AllowAudit
    private Long id;
    @JsonProperty("name")
    @AllowAudit
    private String name;
    @JsonProperty("inn")
    @AllowAudit
    private String inn;
    @JsonProperty("kpp")
    @AllowAudit
    private String kpp;
    @JsonProperty("ogrn")
    @AllowAudit
    private String ogrn;
    @JsonProperty("legalAddress")
    @AllowAudit
    private String legalAddress;
    @JsonProperty("regionCode")
    @AllowAudit
    private String regionCode;
    @JsonProperty("regionName")
    @AllowAudit
    private String regionName;
    @JsonProperty("locality")
    @AllowAudit
    private String locality;
}
