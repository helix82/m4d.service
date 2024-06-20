package su.opencode.digsigcik.synapi.model.ca;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constants.*;

@NoArgsConstructor
@Data
@XmlRootElement(name = XML_ELEMENT_ACC)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = XML_ELEMENT_ACC)
public class CAXml {
    @XmlElement(name = XML_ELEMENT_CA)
    public List<CA> ca;

    @Data
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CA{
        @XmlElement(name = XML_ELEMENT_SHC)
        public SHSystems shSystems;
        @XmlElement(name = XML_ELEMENT_ACCREDITATION_STATUS)
        private AccreditationStatus accreditationStatus;

        @Data
        @XmlRootElement
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class SHSystems{
            @XmlElement(name = XML_ELEMENT_SH_SYSTEM)
            public List<SHSystem> shSystem;

            @Data
            @XmlRootElement
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class SHSystem{
                @XmlElement(name = XML_ELEMENT_KEYS_AUTH_PERSONS)
                private KeysAuthPersons keysAuthPersons;

                @Data
                @XmlRootElement
                @XmlAccessorType(XmlAccessType.FIELD)
                public static class KeysAuthPersons{
                    @XmlElement(name = XML_ELEMENT_KEY)
                    private List<KeyAuthPerson> keyAuthPerson;

                    @Data
                    @XmlRootElement
                    @XmlAccessorType(XmlAccessType.FIELD)
                    public static class KeyAuthPerson{
                        @XmlElement(name = XML_ELEMENT_KEY_ID)
                        private String keyId;
                        @XmlElement(name = XML_ELEMENT_REVIEW_LIST_ADDRESS)
                        private List<Addresses> addresses;
                        @XmlElement(name = XML_ELEMENT_CERTIFICATES)
                        private Certificates certificates;

                        @Data
                        @XmlRootElement
                        @XmlAccessorType(XmlAccessType.FIELD)
                        public static class Addresses{
                            @XmlElement(name = XML_ELEMENT_ADDRESS)
                            private List<String> address;
                        }

                        @Data
                        @XmlRootElement
                        @XmlAccessorType(XmlAccessType.FIELD)
                        public static class Certificates{
                            @XmlElement(name = XML_ELEMENT_CERTIFICATE_DATA)
                            private List<CertificateData> certificateData;

                        }
                    }
                }
            }
        }

        @Data
        @XmlRootElement
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class AccreditationStatus{
            @XmlElement(name = XML_ELEMENT_STATUS)
            private String status;
        }
    }
}
