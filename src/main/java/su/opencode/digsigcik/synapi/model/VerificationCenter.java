package su.opencode.digsigcik.synapi.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@NoArgsConstructor
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class VerificationCenter {
    @XmlElement(name = "Название")
    private String fullName;
    @XmlElement(name = "ЭлектроннаяПочта")
    private String email;
    @XmlElement(name = "КраткоеНазвание")
    private String name;
    @XmlElement(name = "АдресСИнформациейПоУЦ")
    private String urlInfo;
    @XmlElement(name = "Адрес")
    private List<Address> addresses;

}
