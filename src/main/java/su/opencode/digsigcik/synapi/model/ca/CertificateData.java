package su.opencode.digsigcik.synapi.model.ca;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static su.opencode.digsigcik.synapi.util.Constants.*;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name="Certificate_data")
public class CertificateData {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Schema(example = "1", required = true)
        private Long id;
        @XmlElement(name = XML_ELEMENT_IMPRINT)
        @Schema(example = "", required = false)
        private String imprint;
        @XmlElement(name = XML_ELEMENT_SERIAL_NUMBER)
        @Schema(example = "", required = false)
        private String serialNumber;
        @XmlElement(name = XML_ELEMENT_KEY_ID)
        @Schema(example = "", required = false)
        private String keyId;
        @XmlElement(name = XML_ELEMENT_ISSUED_TO)
        @Schema(example = "", required = false)
        @Column(columnDefinition = "Text")
        private String issuedTo;
        @XmlElement(name = XML_ELEMENT_DATE_START)
        @Schema(example = "", required = false)
        private String dateStart;
        @XmlElement(name = XML_ELEMENT_DATE_END)
        @Schema(example = "", required = false)
        private String dateEnd;
        @XmlElement(name = XML_ELEMENT_CERT)
        @Schema(example = "", required = false)
        @Column(columnDefinition = "Text")
        private String certificate;
        @Schema(example = SCHEMA_CA_TYPE_INPUT_EXAMPLE, required = false)
        @Column(name = "type_input")
        private String typeInput;
        @Schema(example = SCHEMA_CA_ACCREDITATION_STATUS_EXAMPLE, required = false)
        @Column(name = "accreditation_status")
        private String accreditationStatus;
}
