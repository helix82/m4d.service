package su.opencode.digsigcik.m4d.dto;

import lombok.Data;
import su.opencode.digsigcik.m4d.model.*;

import java.util.List;

@Data
public class M4dParseResponseDto {
    private M4d m4d;
    private OrganizationDto organisation;
    private Principal principal;
    private PrincipalDetail principalDetail;
    private Representative representative;
    private RepresentativeDetail representativeDetail;
    private List<CredentialDto> credentialList;
    private String xmlString;

    public M4dParseResponseDto(M4d m4d, OrganizationDto organisation, Principal principal, PrincipalDetail principalDetail, Representative representative, RepresentativeDetail representativeDetail, List<CredentialDto> credentialList, String xmlString) {
        this.m4d = m4d;
        this.organisation = organisation;
        this.principal = principal;
        this.principalDetail = principalDetail;
        this.representative = representative;
        this.representativeDetail = representativeDetail;
        this.credentialList = credentialList;
        this.xmlString = xmlString;
    }
}
