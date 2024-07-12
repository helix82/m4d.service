package su.opencode.digsigcik.synapi.dto;

import lombok.Data;
import su.opencode.digsigcik.synapi.enums.RoleEnum;
import su.opencode.digsigcik.synapi.enums.StatusTypeEnum;
import su.opencode.digsigcik.synapi.enums.TypeUserCertificateEnum;

import java.util.List;

@Data
public class AboutMeDto {
    private String name;
    private String surname;
    private String patronymic;
    private RoleEnum role;
    private Boolean ses;
    private List<Certificates> certificates;

    @Data
    public static class Certificates {
        private String thumbprint; //imprint
        private String ownerName; //fio
        private String inn;
        private String organization;
        private String issuerWho;
        private String keyId;
        private String serialNumber;
        private String type; //[LOCAL, CLOUD]
        private TypeUserCertificateEnum signType; //(QUALIFIED, UNQUALIFIED)
        private String validFrom; //dateStart
        private String validTo; //dateEnd
        private String dateStartPrivateKey;
        private String dateEndPrivateKey;
        private List<M4d> m4d;

        @Data
        public static class M4d {
            private Long id;
            private String guid;
            private String internalNumber;
            private Long organisationId;
            private Long principalId;
            private Long representativeId;
            private String startDate;
            private String endDate;
            private StatusTypeEnum status;
            private String sigPath;
            private String mrpaXmlPath;
            private String recalledXmlPath;        }
    }
}
