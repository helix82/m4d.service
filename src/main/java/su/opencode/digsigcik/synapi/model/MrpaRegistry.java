package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("id")
    private Long id;
    @Column(name = "user_id")
    @JsonProperty("userId")
    private Long userId;
    @Column(name = "guid", columnDefinition = "VARCHAR(255)")
    @JsonProperty("guid")
    private String guid;
    @Column(name = "internal_number", columnDefinition = "VARCHAR(255)")
    @JsonProperty("internalNumber")
    private String internalNumber;
    @Column(name = "organisation_id")
    @JsonProperty("organisationId")
    private Long organisationId;
    @Column(name = "principal_id")
    @JsonProperty("principalId")
    private Long principalId;
    @Column(name = "representative_id")
    @JsonProperty("representativeId")
    private Long representativeId;
    @Column(name = "start_date")
    @JsonProperty("startDate")
    private String startDate;
    @Column(name = "end_date")
    @JsonProperty("endDate")
    private String endDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(255)")
    @JsonProperty("status")
    private StatusTypeEnum status;
    @Enumerated(EnumType.STRING)
    @Column(name = "ftx_registration", columnDefinition = "VARCHAR(255)")
    @JsonProperty("ftxRegistration")
    private FtxRegistrationTypeEnum ftxRegistration;
    @Column(name = "sig_path", columnDefinition = "TEXT")
    @JsonProperty("sigPath")
    private String sigPath;
    @Column(name = "mrpa_xml_path", columnDefinition = "TEXT")
    @JsonProperty("mrpaXmlPath")
    private String mrpaXmlPath;
    @Column(name = "recalled_xml_path", columnDefinition = "TEXT")
    @JsonProperty("recalledXmlPath")
    private String recalledXmlPath;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "mrpa_registry_id")
    private Set<MrpaRegistryCredential> mrpaRegistryCredentials = new HashSet<>();
}
