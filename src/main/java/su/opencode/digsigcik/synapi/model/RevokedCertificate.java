package su.opencode.digsigcik.synapi.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "revoked_certificate")
public class RevokedCertificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "imprint")
    private String imprint;
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
}
