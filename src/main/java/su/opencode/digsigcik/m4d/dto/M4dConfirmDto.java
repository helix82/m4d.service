package su.opencode.digsigcik.m4d.dto;

import lombok.Data;

import javax.xml.bind.annotation.*;

import static su.opencode.digsigcik.m4d.util.Constants.*;

@XmlRootElement(name = XML_ELEMENT_MCHD_CONFIRM)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = XML_ELEMENT_MCHD_CONFIRM)
@Data
public class M4dConfirmDto {
    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_CONFIRM_FORMAT_VERSION)
    private String versionFormat; //ПодтвФНС003
    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_CONFIRM_TYPE)
    private String typeConfirm; //ПодтвДовер
    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_CONFIRM_FILE_ID)
    private String idFile; //Квит003_ФНС_ON_EMCHD_20240404_7fe4bb0c-3a8c-4643-ade1-f0e1e25f628d
    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_CONFIRM_GUID)
    private String guid;
    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_CONFIRM_DATE_REGISTERED)
    private String registeredDate;
    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_CONFIRM_OPERATOR_ID)
    private String operatorId;

    @XmlElement(name = XML_ELEMENT_MCHD_CONFIRM_ORGANISATION)
    public Organisation organisation;

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Organisation{
        @XmlAttribute(name = XML_ATTRIBUTE_MCHD_CONFIRM_ORGANISATION_ID)
        private String id;
        @XmlAttribute(name = XML_ATTRIBUTE_MCHD_CONFIRM_ORGANISATION_NAME)
        private String name;
        @XmlAttribute(name = XML_ATTRIBUTE_MCHD_CONFIRM_ORGANISATION_TRADE_NAME)
        private String tradeName;
        @XmlAttribute(name = XML_ATTRIBUTE_MCHD_CONFIRM_ORGANISATION_INN)
        private String inn;
        @XmlAttribute(name = XML_ATTRIBUTE_MCHD_CONFIRM_ORGANISATION_OGRN)
        private String ogrn;
        @XmlElement(name = XML_ELEMENT_MCHD_CONFIRM_SYSTEM_INFO)
        private String infoSystem;
        @XmlElement(name = XML_ELEMENT_MCHD_CONFIRM_SYSTEM_VERSION)
        private String versionSystem;
    }

    @XmlElement(name = XML_ELEMENT_MCHD_CONFIRM_INFO)
    public M4d m4d;

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class M4d{
        @XmlAttribute(name = XML_ATTRIBUTE_MCHD_CONFIRM_INFO_FILE_ID)
        private String idFile; //ON_EMCHD_20240404_7fe4bb0c-3a8c-4643-ade1-f0e1e25f628d
        @XmlAttribute(name = XML_ATTRIBUTE_MCHD_CONFIRM_INFO_GUID)
        private String guid;
        @XmlAttribute(name = XML_ATTRIBUTE_MCHD_CONFIRM_INFO_INTERNAL_NUMBER)
        private String internalNumber;
        @XmlAttribute(name = XML_ATTRIBUTE_MCHD_CONFIRM_INFO_DATE_START)
        private String startDate;
        @XmlAttribute(name = XML_ATTRIBUTE_MCHD_CONFIRM_INFO_DATE_END)
        private String endDate;
        @XmlElement(name = XML_ATTRIBUTE_MCHD_CONFIRM_INFO_SYSTEM_INFO)
        private String infoSystem;
    }
}
