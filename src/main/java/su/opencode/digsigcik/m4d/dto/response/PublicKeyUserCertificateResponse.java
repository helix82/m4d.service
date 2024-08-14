package su.opencode.digsigcik.m4d.dto.response;

import lombok.Data;
import su.opencode.digsigcik.m4d.enums.TypeUserCertificateEnum;

@Data
public class PublicKeyUserCertificateResponse {
    private Long id;
    private String login;
    private String imprint;
    private String keyId;
    private String serialNumber;
    private TypeUserCertificateEnum type;
    private String fio;
    private String inn;
    private String organization;
    private String dateStart;
    private String dateEnd;
    private String dateStartPrivateKey;
    private String dateEndPrivateKey;
    private String issuerWho;
    private String certificate;
    private String originalFilename;
    private Long statementId;
}