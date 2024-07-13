package su.opencode.digsigcik.synapi.dto;

import lombok.Data;
import su.opencode.digsigcik.synapi.enums.RoleEnum;
import su.opencode.digsigcik.synapi.enums.TypeStorageCertificateEnum;
import su.opencode.digsigcik.synapi.enums.TypeUserCertificateEnum;

import java.util.List;

@Data
public class AboutMeDto {
    private String name;
    private String surname;
    private String patronymic;
    private RoleEnum role;
    private Boolean ses;
    public List<Certificates> certificates;

    @Data
    public static class Certificates {
        private String thumbprint; //imprint
        private String ownerName; //fio
        private String inn;
        private String organization;
        private String issuerWho;
        private String keyId;
        private String serialNumber;
        private TypeStorageCertificateEnum type; //[LOCAL, CLOUD]
        private TypeUserCertificateEnum signType; //(QUALIFIED, UNQUALIFIED)
        private String validFrom; //dateStart
        private String validTo; //dateEnd
        private String dateStartPrivateKey;
        private String dateEndPrivateKey;
        private MrpaRegistryFullDTO m4d;
    }
}
