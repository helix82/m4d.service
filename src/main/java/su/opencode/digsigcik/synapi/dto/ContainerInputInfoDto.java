package su.opencode.digsigcik.synapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import su.opencode.digsigcik.synapi.dscp.ContainerTypeEnum;
import su.opencode.digsigcik.synapi.dscp.SignatureTypeEnum;
import su.opencode.digsigcik.synapi.enums.KeySize;
import su.opencode.digsigcik.synapi.model.UserProjection;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerInputInfoDto {
    private String login;
    private String firstname;
    private String lastname;
    private String patronymic;
    private LocalDateTime timestamp;
    private String signatureAlgorithm;
    private KeySize keySize;
    private ContainerTypeEnum containerType;
    private SignatureTypeEnum signatureType;
    private String hashAlgorithm;
    private String pinCode;
    private String pinCodeHashAlgorithm;
    private UserProjection userProjection;
//    private KeyPair keyPair;
//    private X509Certificate cert;
    private boolean signatureDetached = true;
    private byte[] signCheck;
    private byte[] signReport;

}
