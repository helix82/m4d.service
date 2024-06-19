package su.opencode.digsigcik.synapi.model.ca;

import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(name = "ИД сертификата УЦ", example = "1", required = true)
        private Long id;
        @XmlElement(name = "Отпечаток")
        @Schema(name = "Отпечаток", example = "", required = false)
        private String imprint;
        @XmlElement(name = "СерийныйНомер")
        @Schema(name = "Серийный номер", example = "", required = false)
        private String serialNumber;
        @XmlElement(name = "ИдентификаторКлюча")
        @Schema(name = "Идентификатор ключа", example = "", required = false)
        private String keyId;
        @XmlElement(name = "КомуВыдан")
        @Schema(name = "Кому выдан сертификат УЦ", example = "", required = false)
        @Column(columnDefinition = "Text")
        private String issuedTo;
        @XmlElement(name = "ПериодДействияС")
        @Schema(name = "Дата начала действия сертификата УЦ", example = "", required = false)
        private String dateStart;
        @XmlElement(name = "ПериодДействияДо")
        @Schema(name = "Дата окончания действия сертификата УЦ", example = "", required = false)
        private String dateEnd;
        @XmlElement(name = "Данные")
        @Schema(name = "Сертификат УЦ", example = "", required = false)
        @Column(columnDefinition = "Text")
        private String certificate;
        @Schema(name = "Признак ввода сертификата УЦ", example = "Вручную", required = false)
        @Column(name = "type_input")
        private String typeInput;
        @Schema(name = "Статус аккредитации УЦ", example = "Действует", required = false)
        @Column(name = "accreditation_status")
        private String accreditationStatus;
}
