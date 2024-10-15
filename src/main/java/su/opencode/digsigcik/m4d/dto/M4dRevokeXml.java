package su.opencode.digsigcik.m4d.dto;

import lombok.Data;

import javax.xml.bind.annotation.*;

import static su.opencode.digsigcik.m4d.util.Constants.*;

@XmlRootElement(name = XML_ELEMENT_RECALL_FILE)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = XML_ELEMENT_RECALL_FILE)
@Data
public class M4dRevokeXml {
    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_XSI)
    private String xmlnsXsi; //"http://www.w3.org/2001/XMLSchema-instance"
    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_XSD)
    private String xmlnsXsd; //"http://www.w3.org/2001/XMLSchema"
    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_FORMAT_VERSION)
    private String versionFormat; //001
    @XmlElement(name = XML_ELEMENT_DOCUMENT)
    public Document document;

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Document{
        @XmlElement(name = XML_ELEMENT_RECALL_INFO)
        public RecallInfo recallInfo;

        @XmlRootElement
        @XmlAccessorType(XmlAccessType.FIELD)
        @Data
        public static class RecallInfo {
            @XmlAttribute(name = XML_ATTRIBUTE_RECALL_GUID)
            private String guid;
            @XmlAttribute(name = XML_ATTRIBUTE_RECALL_ARGUMENT)
            private String argument;
        }

        @XmlElement(name = XML_ELEMENT_RECALL_WHO)
        public RecallWho recallWho;

        @XmlRootElement
        @XmlAccessorType(XmlAccessType.FIELD)
        @Data
        public static class RecallWho {
            @XmlElement(name = XML_ELEMENT_RECALL_PRINCIPALS)
            public Principal principal;

            @XmlRootElement
            @XmlAccessorType(XmlAccessType.FIELD)
            @Data
            public static class Principal{
                @XmlElement(name = XML_ELEMENT_RECALL_LEGAL_ENTITY)
                public LegalEntity legalEntity;

                @XmlRootElement
                @XmlAccessorType(XmlAccessType.FIELD)
                @Data
                public static class LegalEntity{
                    @XmlAttribute(name = XML_ATTRIBUTE_RECALL_ORGANISATION_NAME)
                    private String name;
                    @XmlAttribute(name = XML_ATTRIBUTE_RECALL_ORGANISATION_INN)
                    private String inn;
                    @XmlAttribute(name = XML_ATTRIBUTE_RECALL_ORGANISATION_KPP)
                    private String kpp;
                    @XmlAttribute(name = XML_ATTRIBUTE_RECALL_ORGANISATION_OGRN)
                    private String ogrn;
                }
            }

            @XmlElement(name = XML_ELEMENT_RECALL_REPRESENTATIVE)
            public Representative representative;

            @XmlRootElement
            @XmlAccessorType(XmlAccessType.FIELD)
            @Data
            public static class Representative{
                @XmlElement(name = XML_ELEMENT_RECALL_DETAIL_REPRESENTATIVE)
                public DetailRepresentative detailRepresentative;

                @XmlRootElement
                @XmlAccessorType(XmlAccessType.FIELD)
                @Data
                public static class DetailRepresentative{
                    @XmlAttribute(name = XML_ATTRIBUTE_RECALL_INN)
                    private String inn;
                    @XmlAttribute(name = XML_ATTRIBUTE_RECALL_SNILS)
                    private String snils;

                    @XmlElement(name = XML_ELEMENT_RECALL_FIO)
                    public FioRepresentative fioRepresentative;

                    @XmlRootElement
                    @XmlAccessorType(XmlAccessType.FIELD)
                    @Data
                    public static class FioRepresentative{
                        @XmlAttribute(name = XML_ATTRIBUTE_RECALL_FIO_SURNAME)
                        private String surname;
                        @XmlAttribute(name = XML_ATTRIBUTE_RECALL_FIO_NAME)
                        private String name;
                        @XmlAttribute(name = XML_ATTRIBUTE_RECALL_FIO_PATRONYMIC)
                        private String patronymic;
                    }
                }
            }
        }

    }
}
