package su.opencode.digsigcik.synapi.model;

import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.synapi.enums.FtxRegistrationTypeEnum;
import su.opencode.digsigcik.synapi.enums.StatusTypeEnum;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Table(name="mrpa_registry")
@Transactional
// Реестр МЧД
public class MrpaRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "guid", columnDefinition = "VARCHAR(50)")
    private String guid;
    @Column(name = "internal_number", columnDefinition = "VARCHAR(50)")
    private String internalNumber;
    @ManyToOne
    @JoinColumn(name = "organisation_id", referencedColumnName="id")
    private Organisation organisationId;
    @ManyToOne
    @JoinColumn(name = "principal_id", referencedColumnName="id")
    private Principal principalId;
    @ManyToOne
    @JoinColumn(name = "representative_id", referencedColumnName="id")
    private Representative representativeId;
    @Column(name = "start_date")
    private Timestamp startDate;
    @Column(name = "end_date")
    private Timestamp endDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(50)")
    private StatusTypeEnum status;
    //private String status;
    @Enumerated(EnumType.STRING)
    @Column(name = "ftx_registration", columnDefinition = "VARCHAR(50)")
    private FtxRegistrationTypeEnum ftxRegistration;
    //private String ftxRegistration;
    @Column(name = "sig_path", columnDefinition = "VARCHAR(50)")
    private String sigPath;
    @Column(name = "mrpa_xml_path", columnDefinition = "VARCHAR(50)")
    private String mrpaXmlPath;
    @Column(name = "recalled_xml_path", columnDefinition = "VARCHAR(50)")
    private String recalledXmlPath;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "mrpa_registry_credential",
            joinColumns = @JoinColumn(name = "mrpa_registry_id", referencedColumnName="id"),
            inverseJoinColumns = @JoinColumn(name = "credential_id", referencedColumnName="id")
    )
    private Set<Credential> credentials  = new HashSet<>();
}
