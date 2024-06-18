package su.opencode.digsigcik.synapi.model.ca;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constants.XML_ELEMENT_CA;

@NoArgsConstructor
@Data
@XmlRootElement(name = "АккредитованныеУдостоверяющиеЦентры")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "АккредитованныеУдостоверяющиеЦентры")
public class CAXml {
    @XmlElement(name = XML_ELEMENT_CA)
    public List<CA> ca;

    @Data
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CA{
        @XmlElement(name = "ПрограммноАппаратныеКомплексы")
        public SHSystems shSystems;
        @XmlElement(name = "СтатусАккредитации")
        private AccreditationStatus accreditationStatus;

        @Data
        @XmlRootElement
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class SHSystems{
            @XmlElement(name = "ПрограммноАппаратныйКомплекс")
            public List<SHSystem> shSystem;

            @Data
            @XmlRootElement
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class SHSystem{
                @XmlElement(name = "КлючиУполномоченныхЛиц")
                private KeysAuthPersons keysAuthPersons;

                @Data
                @XmlRootElement
                @XmlAccessorType(XmlAccessType.FIELD)
                public static class KeysAuthPersons{
                    @XmlElement(name = "Ключ")
                    private List<KeyAuthPerson> keyAuthPerson;

                    @Data
                    @XmlRootElement
                    @XmlAccessorType(XmlAccessType.FIELD)
                    public static class KeyAuthPerson{
                        @XmlElement(name = "АдресаСписковОтзыва")
                        private List<Addresses> addresses;
                        @XmlElement(name = "Сертификаты")
                        private Certificates certificates;

                        @Data
                        @XmlRootElement
                        @XmlAccessorType(XmlAccessType.FIELD)
                        public static class Addresses{
                            @XmlElement(name = "Адрес")
                            private List<String> address;
                        }

                        @Data
                        @XmlRootElement
                        @XmlAccessorType(XmlAccessType.FIELD)
                        public static class Certificates{
                            @XmlElement(name = "ДанныеСертификата")
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
            @XmlElement(name = "Статус")
            private String status;
        }
    }
}
