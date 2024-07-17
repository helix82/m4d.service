package su.opencode.digsigcik.synapi.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "trusted_certificate")
public class TrustedCertificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "imprint")
    private String imprint;
    @Column(name = "serial_number")
    private String serialNumber;
    @Column(name = "key_id")
    private String keyId;
    @Column(name = "issued_to", columnDefinition = "Text")
    private String issuedTo;
    @Column(name = "date_start")
    private String dateStart;
    @Column(name = "date_end")
    private String dateEnd;
    @Column(name = "certificate", columnDefinition = "Text")
    private String certificate;
    @Column(name = "original_filename")
    private String originalFilename;
    @Column(name = "type_input")
    private String typeInput;
    @Column(name = "accreditation_status")
    private String accreditationStatus;
}
