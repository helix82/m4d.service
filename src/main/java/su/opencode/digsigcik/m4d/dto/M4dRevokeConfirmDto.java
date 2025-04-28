package su.opencode.digsigcik.m4d.dto;

import lombok.Data;

import javax.xml.bind.annotation.*;

import static su.opencode.digsigcik.m4d.util.Constants.*;

@XmlRootElement(name = XML_ELEMENT_MCHD_REVOKE_CONFIRM)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = XML_ELEMENT_MCHD_REVOKE_CONFIRM)
@Data
public class M4dRevokeConfirmDto {
    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_REVOKE_CONFIRM_FORMAT_VERSION)
    private String versionFormat; //ПодтвФНС003
    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_REVOKE_CONFIRM_TYPE)
    private String typeConfirm; //ПодтвЗаявлОтмена
    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_REVOKE_CONFIRM_FILE_ID)
    private String idFile; //Квит003_ФНС_ЗаявОтм_eecbffec-e37f-4aa4-a135-896241dd8d71
    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_REVOKE_CONFIRM_GUID)
    private String guid;
    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_REVOKE_CONFIRM_DATE_REGISTERED)
    private String registeredDate;
    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_REVOKE_CONFIRM_OPERATOR_ID)
    private String operatorId;

    @XmlElement(name = XML_ELEMENT_MCHD_REVOKE_CONFIRM_ORGANISATION)
    public M4dConfirmDto.Organisation organisation;

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Organisation{
        @XmlAttribute(name = XML_ATTRIBUTE_MCHD_REVOKE_CONFIRM_ORGANISATION_ID)
        private String id;
        @XmlAttribute(name = XML_ATTRIBUTE_MCHD_REVOKE_CONFIRM_ORGANISATION_NAME)
        private String name;
        @XmlAttribute(name = XML_ATTRIBUTE_MCHD_REVOKE_CONFIRM_ORGANISATION_TRADE_NAME)
        private String tradeName;
        @XmlAttribute(name = XML_ATTRIBUTE_MCHD_REVOKE_CONFIRM_ORGANISATION_INN)
        private String inn;
        @XmlAttribute(name = XML_ATTRIBUTE_MCHD_REVOKE_CONFIRM_ORGANISATION_OGRN)
        private String ogrn;
        @XmlElement(name = XML_ELEMENT_MCHD_REVOKE_CONFIRM_SYSTEM_INFO)
        private String infoSystem;
        @XmlElement(name = XML_ELEMENT_MCHD_REVOKE_CONFIRM_SYSTEM_VERSION)
        private String versionSystem;
    }

    @XmlElement(name = XML_ELEMENT_MCHD_REVOKE_CONFIRM_INFO)
    public M4dRevoke m4d;

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class M4dRevoke{
        @XmlAttribute(name = XML_ATTRIBUTE_MCHD_REVOKE_CONFIRM_INFO_FILE_HASH)
        private String hashFile;
        @XmlAttribute(name = XML_ATTRIBUTE_MCHD_REVOKE_CONFIRM_INFO_GUID)
        private String guid;
    }
}
