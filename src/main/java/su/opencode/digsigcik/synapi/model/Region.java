package su.opencode.digsigcik.synapi.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@NoArgsConstructor
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Region {
    @XmlElement(name = "Код")
    private Integer code;
    @XmlElement(name = "Название")
    private String name;
}
