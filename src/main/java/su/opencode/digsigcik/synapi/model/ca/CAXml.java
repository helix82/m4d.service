package su.opencode.digsigcik.synapi.model.ca;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Data
@XmlRootElement(name = "АккредитованныеУдостоверяющиеЦентры")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "АккредитованныеУдостоверяющиеЦентры")
public class CAXml {
    @XmlElement(name = "УдостоверяющийЦентр")
    private List<CA> ca;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class CA{
        @XmlElement(name = "ПрограммноАппаратныеКомплексы")
        private SHSystems shSystems;
        @XmlElement(name = "СтатусАккредитации")
        private AccreditationStatus accreditationStatus;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class SHSystems{
        @XmlElement(name = "ПрограммноАппаратныйКомплекс")
        private List<SHSystem> shSystem;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class SHSystem{
        @XmlElement(name = "КлючиУполномоченныхЛиц")
        private KeysAuthPersons keysAuthPersons;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class KeysAuthPersons{
        @XmlElement(name = "Ключ")
        private List<KeyAuthPerson> keyAuthPerson;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class KeyAuthPerson{
        @XmlElement(name = "АдресаСписковОтзыва")
        private List<Addresses> addresses;
        @XmlElement(name = "Сертификаты")
        private Certificates certificates;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class Addresses{
        @XmlElement(name = "Адрес")
        private String address;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class Certificates{
        @XmlElement(name = "ДанныеСертификата")
        private List<CertificateData> certificateData;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class CertificateData{
        @XmlElement(name = "КомуВыдан")
        private String issuedTo;
        @XmlElement(name = "ПериодДействияС")
        private Date dateStart;
        @XmlElement(name = "ПериодДействияДо")
        private Date dateEnd;
        @XmlElement(name = "Данные")
        private String certificate;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class AccreditationStatus{
        @XmlElement(name = "Статус")
        private String status;
    }
}
