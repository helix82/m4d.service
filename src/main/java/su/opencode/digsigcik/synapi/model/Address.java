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
public class Address {
    @XmlElement(name = "Страна")
    private String country;
    @XmlElement(name = "Регион")
    private List<Region> regions;
    @XmlElement(name = "Индекс")
    private String index;
    @XmlElement(name = "УлицаДом")
    private String street;
    @XmlElement(name = "Город")
    private String city;
}
