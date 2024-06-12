package su.opencode.digsigcik.synapi.dto;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Доверенность")
public class MrpaRegistryFullXML {
    @XmlAttribute(name = "xmlns:xsi")
    private String xmlnsXsi; //"http://www.w3.org/2001/XMLSchema-instance"
    @XmlAttribute(name = "xmlns:xsd")
    private String xmlnsXsd; //"http://www.w3.org/2001/XMLSchema"
    @XmlAttribute(name = "xmlns")
    private String xmlns; //"urn://x-artefacts/EMCHD_1"
    @XmlAttribute(name = "ВерсФорм")
    private String versionFormat; //EMCHD_1
    @XmlAttribute(name = "ПрЭлФорм")
    private String elementsFormat; //00000000
    @XmlAttribute(name = "ИдФайл")
    private String idFile; //ON_EMCHD_20240516_75655b80-cc58-4a2b-b244-8334eb895a2f

    @XmlElement(name = "Документ")
    public List<Document> document;

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Document{
        @XmlElement(name = "Довер")
        public List<PowerAttorneys> powerAttorneys;

        @XmlRootElement
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class PowerAttorneys{
            @XmlElement(name = "СвДов")
            public Mrpa mrpa;

            @XmlRootElement
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class Mrpa{
                @XmlAttribute(name = "ВидДовер")
                private String typePowerAttorney; //1
                @XmlAttribute(name = "ПрПередов")
                private String retrust; //3
                @XmlAttribute(name = "ВнНомДовер")
                private String internalNumber;
                @XmlAttribute(name = "НомДовер")
                private String guid;
                @XmlAttribute(name = "ДатаВыдДовер")
                private String startDate;
                @XmlAttribute(name = "СрокДейст")
                private String endDate;
                @XmlElement(name = "СведСист")
                private String infoSystem;
            }

            @XmlElement(name = "СвДоверит")
            public InfoPrincipals principals;

            @XmlRootElement
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class InfoPrincipals{
                @XmlAttribute(name = "ТипДоверит")
                private String typePrincipal;//1
                @XmlElement(name = "Доверит")
                public List<InfoPrincipal> principal;

                @XmlRootElement
                @XmlAccessorType(XmlAccessType.FIELD)
                public static class InfoPrincipal{
                    @XmlElement(name = "РосОргДовер")
                    public LegalEntity legalEntity;

                    @XmlRootElement
                    @XmlAccessorType(XmlAccessType.FIELD)
                    public static class LegalEntity{
                        @XmlAttribute(name = "ЕИОУК")
                        private String managementCompany; //0
                        @XmlAttribute(name = "ЕИОФЛ")
                        private String individual; //1
                        @XmlAttribute(name = "ЕИОИП")
                        private String individualEntrepreneur; //0
                        @XmlElement(name = "СвРосОрг")
                        private Organisation organisation;

                        @XmlRootElement
                        @XmlAccessorType(XmlAccessType.FIELD)
                        public static class Organisation{
                            @XmlAttribute(name = "НаимОрг")
                            private String name;
                            @XmlAttribute(name = "ИННЮЛ")
                            private String inn;
                            @XmlAttribute(name = "КПП")
                            private String kpp;
                            @XmlAttribute(name = "ОГРН")
                            private String ogrn;
                            @XmlElement(name = "АдрРег")
                            public AddressRegion addressRegion;

                            @XmlRootElement
                            @XmlAccessorType(XmlAccessType.FIELD)
                            public static class AddressRegion{
                                @XmlAttribute(name = "Регион")
                                private String regionCode;
                                @XmlElement(name = "АдрРФ")
                                public String legalAddress;
                            }
                        }

                        @XmlElement(name = "ЛицоБезДов")
                        public PersonPrincipal principal;

                        @XmlRootElement
                        @XmlAccessorType(XmlAccessType.FIELD)
                        public static class PersonPrincipal{
                            @XmlAttribute(name = "ПолнЮЛ")
                            private String typeCredential; //1

                            @XmlElement(name = "СвФл")
                            public Principal principal;

                            @XmlRootElement
                            @XmlAccessorType(XmlAccessType.FIELD)
                            public static class Principal{
                                @XmlAttribute(name = "ИННФЛ")
                                private String inn;
                                @XmlAttribute(name = "СНИЛС")
                                private String snils;
                                @XmlAttribute(name = "Должность")
                                private String jobTitle;

                                @XmlElement(name = "СведФЛ")
                                public FioPrincipal fioPrincipal;

                                @XmlRootElement
                                @XmlAccessorType(XmlAccessType.FIELD)
                                public static class FioPrincipal{
                                    @XmlElement(name = "ФИО")
                                    public Fio fio;

                                    @XmlRootElement
                                    @XmlAccessorType(XmlAccessType.FIELD)
                                    public static class Fio{
                                        @XmlAttribute(name = "Фамилия")
                                        private String surname;
                                        @XmlAttribute(name = "Имя")
                                        private String name;
                                        @XmlAttribute(name = "Отчество")
                                        private String patronymic;
                                    }
                                }

                            }
                        }
                    }
                }
            }

            @XmlElement(name = "СвУпПред")
            public InfoRepresentative infoRepresentative;

            @XmlRootElement
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class InfoRepresentative{
                @XmlAttribute(name = "ТипПред")
                private String typeRepresentative; //3

                @XmlElement(name = "Пред")
                public Representative representative;

                @XmlRootElement
                @XmlAccessorType(XmlAccessType.FIELD)
                public static class Representative{
                    @XmlElement(name = "СведФизЛ")
                    public DetailRepresentative detailRepresentative;

                    @XmlRootElement
                    @XmlAccessorType(XmlAccessType.FIELD)
                    public static class DetailRepresentative{
                        @XmlAttribute(name = "ИННФЛ")
                        private String inn;
                        @XmlAttribute(name = "СНИЛС")
                        private String snils;
                        @XmlElement(name = "СведФЛ")
                        public InfoIndividual infoIndividual;

                        @XmlRootElement
                        @XmlAccessorType(XmlAccessType.FIELD)
                        public static class InfoIndividual{
                            @XmlAttribute(name = "ДатаРожд")
                            private String birthDate;
                            @XmlElement(name = "ФИО")
                            public FioRepresentative fioRepresentative;

                            @XmlRootElement
                            @XmlAccessorType(XmlAccessType.FIELD)
                            public static class FioRepresentative{
                                @XmlAttribute(name = "Фамилия")
                                private String surname;
                                @XmlAttribute(name = "Имя")
                                private String name;
                                @XmlAttribute(name = "Отчество")
                                private String patronymic;
                            }

                            @XmlElement(name = "УдЛичнФЛ")
                            public RepresentativePassport representativePassport;

                            @XmlRootElement
                            @XmlAccessorType(XmlAccessType.FIELD)
                            public static class RepresentativePassport{
                                @XmlAttribute(name = "КодВидДок")
                                private String typeDocument; //21
                                @XmlAttribute(name = "СерНомДок")
                                private String seriesNumber;
                                @XmlAttribute(name = "ДатаДок")
                                private String dateIssue;
                                @XmlAttribute(name = "ВыдДок")
                                private String whoIssue;
                                @XmlAttribute(name = "КодВыдДок")
                                private String departmentCode;
                            }
                        }
                    }
                }
            }

            @XmlElement(name = "СвПолн")
            public Credential credential;

            @XmlRootElement
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class Credential{
                @XmlAttribute(name = "ТипПолн")
                private String typeCredential; //0
                @XmlAttribute(name = "ПрСовмПолн")
                private String implCredential; //1
                @XmlAttribute(name = "ПрУтрПолн")
                private String lossCredential; //1
                @XmlElement(name = "ТекстПолн")
                public String name;
            }
        }
    }
}
