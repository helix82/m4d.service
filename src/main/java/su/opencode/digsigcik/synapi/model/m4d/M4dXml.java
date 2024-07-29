package su.opencode.digsigcik.synapi.model.m4d;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constants.*;

@NoArgsConstructor
@Data
@XmlRootElement(name = XML_ELEMENT_MCHD)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = XML_ELEMENT_MCHD)
public class M4dXml {
//    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_XSI)
//    private String xmlnsXsi; //"http://www.w3.org/2001/XMLSchema-instance"
//    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_XSD)
//    private String xmlnsXsd; //"http://www.w3.org/2001/XMLSchema"
//    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_XMLNS)
//    private String xmlns; //"urn://x-artefacts/EMCHD_1"
    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_FORMAT_VERSION)
    private String versionFormat; //EMCHD_1
    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_FORMAT_ELEMENTS)
    private String elementsFormat; //00000000
    @XmlAttribute(name = XML_ATTRIBUTE_MCHD_FILE_ID)
    private String idFile; //ON_EMCHD_20240516_75655b80-cc58-4a2b-b244-8334eb895a2f

    @XmlElement(name = XML_ELEMENT_DOCUMENT)
    public Document document;

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Document{
        @XmlElement(name = XML_ELEMENT_MCHD_SHORT)
        public PowerAttorneys powerAttorneys;

        @XmlRootElement
        @XmlAccessorType(XmlAccessType.FIELD)
        @Data
        public static class PowerAttorneys{
            @XmlElement(name = XML_ELEMENT_MRPA)
            public Mrpa mrpa;

            @XmlRootElement
            @XmlAccessorType(XmlAccessType.FIELD)
            @Data
            public static class Mrpa{
                @XmlAttribute(name = XML_ATTRIBUTE_MRPA_TYPE)
                private String typePowerAttorney; //1
                @XmlAttribute(name = XML_ATTRIBUTE_MRPA_RETRUST)
                private String retrust; //3
                @XmlAttribute(name = XML_ATTRIBUTE_MRPA_INTERNAL_NUMBER)
                private String internalNumber;
                @XmlAttribute(name = XML_ATTRIBUTE_MRPA_GUID)
                private String guid;
                @XmlAttribute(name = XML_ATTRIBUTE_MRPA_DATE_START)
                private String startDate;
                @XmlAttribute(name = XML_ATTRIBUTE_MRPA_DATE_END)
                private String endDate;
                @XmlElement(name = XML_ATTRIBUTE_MRPA_SYSTEM_INFO)
                private String infoSystem;
            }

            @XmlElement(name = XML_ELEMENT_PRINCIPALS)
            public InfoPrincipals principals;

            @XmlRootElement
            @XmlAccessorType(XmlAccessType.FIELD)
            @Data
            public static class InfoPrincipals{
                @XmlAttribute(name = XML_ATTRIBUTE_PRINCIPAL_TYPE)
                private String typePrincipal;//1
                @XmlElement(name = XML_ELEMENT_INFO_PRINCIPAL)
                public InfoPrincipal principal;

                @XmlRootElement
                @XmlAccessorType(XmlAccessType.FIELD)
                @Data
                public static class InfoPrincipal{
                    @XmlElement(name = XML_ELEMENT_LEGAL_ENTITY)
                    public LegalEntity legalEntity;

                    @XmlRootElement
                    @XmlAccessorType(XmlAccessType.FIELD)
                    @Data
                    public static class LegalEntity{
                        @XmlAttribute(name = XML_ATTRIBUTE_LEGAL_ENTITY_MANAGEMENT_COMPANY)
                        private String managementCompany; //0
                        @XmlAttribute(name = XML_ATTRIBUTE_LEGAL_ENTITY_INDIVIDUAL)
                        private String individual; //1
                        @XmlAttribute(name = XML_ATTRIBUTE_LEGAL_ENTITY_INDIVIDUAL_ENTR)
                        private String individualEntrepreneur; //0
                        @XmlElement(name = XML_ELEMENT_ORGANISATION)
                        public Organisation organisation;

                        @XmlRootElement
                        @XmlAccessorType(XmlAccessType.FIELD)
                        @Data
                        public static class Organisation{
                            @XmlAttribute(name = XML_ATTRIBUTE_ORGANISATION_NAME)
                            private String name;
                            @XmlAttribute(name = XML_ATTRIBUTE_ORGANISATION_INN)
                            private String inn;
                            @XmlAttribute(name = XML_ATTRIBUTE_ORGANISATION_KPP)
                            private String kpp;
                            @XmlAttribute(name = XML_ATTRIBUTE_ORGANISATION_OGRN)
                            private String ogrn;
                            @XmlElement(name = XML_ELEMENT_ORGANISATION_ADDRESS_REGION)
                            public AddressRegion addressRegion;

                            @XmlRootElement
                            @XmlAccessorType(XmlAccessType.FIELD)
                            @Data
                            public static class AddressRegion{
                                @XmlAttribute(name = XML_ATTRIBUTE_ORGANISATION_ADDRESS_REGION)
                                private String regionCode;
                                @XmlElement(name = XML_ELEMENT_ORGANISATION_ADDRESS)
                                public String legalAddress;
                            }
                        }

                        @XmlElement(name = XML_ELEMENT_PERSON_PRINCIPAL)
                        public PersonPrincipal principal;

                        @XmlRootElement
                        @XmlAccessorType(XmlAccessType.FIELD)
                        @Data
                        public static class PersonPrincipal{
                            @XmlAttribute(name = XML_ATTRIBUTE_PERSON_PRINCIPAL_TYPE)
                            private String typePrincipal; //1

                            @XmlElement(name = XML_ELEMENT_PRINCIPAL)
                            public Principal principal;

                            @XmlRootElement
                            @XmlAccessorType(XmlAccessType.FIELD)
                            @Data
                            public static class Principal{
                                @XmlAttribute(name = XML_ATTRIBUTE_INN)
                                private String inn;
                                @XmlAttribute(name = XML_ATTRIBUTE_SNILS)
                                private String snils;
                                @XmlAttribute(name = XML_ATTRIBUTE_JOB_TITLE)
                                private String jobTitle;

                                @XmlElement(name = XML_ELEMENT_PRINCIPAL_DETAIL)
                                public FioPrincipal fioPrincipal;

                                @XmlRootElement
                                @XmlAccessorType(XmlAccessType.FIELD)
                                @Data
                                public static class FioPrincipal{
                                    @XmlElement(name = XML_ELEMENT_FIO)
                                    public Fio fio;

                                    @XmlRootElement
                                    @XmlAccessorType(XmlAccessType.FIELD)
                                    @Data
                                    public static class Fio{
                                        @XmlAttribute(name = XML_ATTRIBUTE_FIO_SURNAME)
                                        private String surname;
                                        @XmlAttribute(name = XML_ATTRIBUTE_FIO_NAME)
                                        private String name;
                                        @XmlAttribute(name = XML_ATTRIBUTE_FIO_PATRONYMIC)
                                        private String patronymic;
                                    }
                                }

                            }
                        }
                    }
                }
            }

            @XmlElement(name = XML_ELEMENT_INFO_REPRESENTATIVE)
            public InfoRepresentative infoRepresentative;

            @XmlRootElement
            @XmlAccessorType(XmlAccessType.FIELD)
            @Data
            public static class InfoRepresentative{
                @XmlAttribute(name = XML_ATTRIBUTE_INFO_REPRESENTATIVE_TYPE)
                private String typeRepresentative; //3

                @XmlElement(name = XML_ELEMENT_REPRESENTATIVE)
                public Representative representative;

                @XmlRootElement
                @XmlAccessorType(XmlAccessType.FIELD)
                @Data
                public static class Representative{
                    @XmlElement(name = XML_ELEMENT_DETAIL_REPRESENTATIVE)
                    public DetailRepresentative detailRepresentative;

                    @XmlRootElement
                    @XmlAccessorType(XmlAccessType.FIELD)
                    @Data
                    public static class DetailRepresentative{
                        @XmlAttribute(name = XML_ATTRIBUTE_INN)
                        private String inn;
                        @XmlAttribute(name = XML_ATTRIBUTE_SNILS)
                        private String snils;
                        @XmlElement(name = XML_ELEMENT_INFO_INDIVIDUAL)
                        public InfoIndividual infoIndividual;

                        @XmlRootElement
                        @XmlAccessorType(XmlAccessType.FIELD)
                        @Data
                        public static class InfoIndividual{
                            @XmlAttribute(name = XML_ATTRIBUTE_INFO_INDIVIDUAL_BIRTHDATE)
                            private String birthDate;
                            @XmlElement(name = XML_ELEMENT_FIO)
                            public FioRepresentative fioRepresentative;

                            @XmlRootElement
                            @XmlAccessorType(XmlAccessType.FIELD)
                            @Data
                            public static class FioRepresentative{
                                @XmlAttribute(name = XML_ATTRIBUTE_FIO_SURNAME)
                                private String surname;
                                @XmlAttribute(name = XML_ATTRIBUTE_FIO_NAME)
                                private String name;
                                @XmlAttribute(name = XML_ATTRIBUTE_FIO_PATRONYMIC)
                                private String patronymic;
                            }

                            @XmlElement(name = XML_ELEMENT_REPRESENTATIVE_PASSPORT)
                            public RepresentativePassport representativePassport;

                            @XmlRootElement
                            @XmlAccessorType(XmlAccessType.FIELD)
                            @Data
                            public static class RepresentativePassport{
                                @XmlAttribute(name = XML_ATTRIBUTE_REPRESENTATIVE_PASSPORT_TYPE)
                                private String typeDocument; //21
                                @XmlAttribute(name = XML_ATTRIBUTE_REPRESENTATIVE_PASSPORT_SERIES_NUMBER)
                                private String seriesNumber;
                                @XmlAttribute(name = XML_ATTRIBUTE_REPRESENTATIVE_PASSPORT_DATE)
                                private String dateIssue;
                                @XmlAttribute(name = XML_ATTRIBUTE_REPRESENTATIVE_PASSPORT_WHO)
                                private String whoIssue;
                                @XmlAttribute(name = XML_ATTRIBUTE_REPRESENTATIVE_PASSPORT_CODE)
                                private String departmentCode;
                            }
                        }
                    }
                }
            }

            @XmlElement(name = XML_ELEMENT_CREDENTIAL)
            public Credentials credential;

            @XmlRootElement
            @XmlAccessorType(XmlAccessType.FIELD)
            @Data
            public static class Credentials{
                @XmlAttribute(name = XML_ATTRIBUTE_CREDENTIAL_TYPE)
                private String typeCredential; //0
                @XmlAttribute(name = XML_ATTRIBUTE_CREDENTIAL_IMPL)
                private String implCredential; //1
                @XmlAttribute(name = XML_ATTRIBUTE_CREDENTIAL_LOSS)
                private String lossCredential; //1
                @XmlElement(name = XML_ELEMENT_CREDENTIAL_LIST)
                public List<Credential> credentials;

                @XmlRootElement
                @XmlAccessorType(XmlAccessType.FIELD)
                @Data
                public static class Credential{
                    @XmlAttribute(name = XML_ATTRIBUTE_CREDENTIAL_CODE)
                    private String code;
                    @XmlAttribute(name = XML_ATTRIBUTE_CREDENTIAL_NAME)
                    private String name;
                }
            }
        }
    }
}
