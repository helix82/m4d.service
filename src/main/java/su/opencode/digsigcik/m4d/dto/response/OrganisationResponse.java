package su.opencode.digsigcik.m4d.dto.response;

import lombok.Data;

@Data
public class OrganisationResponse {
    private Long id;
    private String name;
    private String inn;
    private String kpp;
    private String ogrn;
    private String legalAddress;
    private String regionCode;
}
