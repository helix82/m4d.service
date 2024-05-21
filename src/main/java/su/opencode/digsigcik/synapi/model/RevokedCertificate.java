package su.opencode.digsigcik.synapi.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "revoked_certificate")
@Transactional
public class RevokedCertificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cert_centr_name_id")
    private Long certCentrNameId;
    @Column(name = "validity_period")
    private Timestamp validityPeriod;
    @Column(name = "type_of_addition", columnDefinition = "VARCHAR(50)")
    private String typeOfAddition;
    @Column(name = "cert_center_type", columnDefinition = "VARCHAR(50)")
    private String certCenterType;
    @Column(name = "file_path", columnDefinition = "VARCHAR(50)")
    private String filePath;
}
