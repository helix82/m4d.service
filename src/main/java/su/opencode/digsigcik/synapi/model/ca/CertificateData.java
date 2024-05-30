package su.opencode.digsigcik.synapi.model.ca;

import lombok.Data;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name="Certificate_data")
public class CertificateData {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @XmlElement(name = "КомуВыдан")
        @Column(columnDefinition = "Text")
        private String issuedTo;
        @XmlElement(name = "ПериодДействияС")
        private String dateStart;
        @XmlElement(name = "ПериодДействияДо")
        private String dateEnd;
        @XmlElement(name = "Данные")
        @Column(columnDefinition = "Text")
        private String certificate;
}
