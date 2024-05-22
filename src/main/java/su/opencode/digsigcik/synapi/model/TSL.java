package su.opencode.digsigcik.synapi.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.TimeZone;

@NoArgsConstructor
@Data
@XmlRootElement(name = "АккредитованныеУдостоверяющиеЦентры")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "АккредитованныеУдостоверяющиеЦентры")
public class TSL {
    @XmlAttribute(name = "xmlns:xsi")
    private String xmlns;
    @XmlAttribute(name = "xsi:noNamespaceSchemaLocation")
    private String xsi;
    @XmlElement(name = "Версия")
    private String version;
    @XmlElement(name = "Дата")
    private TimeZone date;
    @XmlElementWrapper(name = "АккредитованныеУдостоверяющиеЦентры")
    @XmlElement(name = "УдостоверяющийЦентр")
    private List<VerificationCenter> verificationCenters;
}
