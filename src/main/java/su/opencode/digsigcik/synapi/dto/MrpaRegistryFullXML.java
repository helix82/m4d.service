package su.opencode.digsigcik.synapi.dto;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "Доверенность")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Доверенность")
public class MrpaRegistryFullXML {
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

                            @XmlAttribute(name = "СвФл")
                            public Principal principal;

                            @XmlRootElement
                            @XmlAccessorType(XmlAccessType.FIELD)
                            public static class Principal{
                                @XmlAttribute(name = "ИННФЛ")
                                private String inn;

                            }
                        }
                    }
                }
            }

        }
    }
}
