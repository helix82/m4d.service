package su.opencode.digsigcik.synapi.model;

import lombok.Data;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.synapi.enums.FtxRegistrationTypeEnum;
import su.opencode.digsigcik.synapi.enums.StatusTypeEnum;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name="mrpa_registry")
@Transactional
// Реестр МЧД
public class MrpaRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "guid", columnDefinition = "VARCHAR(255)")
    private String guid;
    @Column(name = "internal_number", columnDefinition = "VARCHAR(255)")
    private String internalNumber;
    @Column(name = "organisation_id")
    private Long organisationId;
    @Column(name = "principal_id")
    private Long principalId;
    @Column(name = "representative_id")
    private Long representativeId;
    @Column(name = "start_date")
    private String startDate;
    @Column(name = "end_date")
    private String endDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(255)")
    private StatusTypeEnum status;
    @Enumerated(EnumType.STRING)
    @Column(name = "ftx_registration", columnDefinition = "VARCHAR(255)")
    private FtxRegistrationTypeEnum ftxRegistration;
    @Column(name = "sig_path", columnDefinition = "TEXT")
    private String sigPath;
    @Column(name = "mrpa_xml_path", columnDefinition = "TEXT")
    private String mrpaXmlPath;
    @Column(name = "recalled_xml_path", columnDefinition = "TEXT")
    private String recalledXmlPath;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "mrpa_registry_id")
    private Set<MrpaRegistryCredential> mrpaRegistryCredentials = new HashSet<>();
}
