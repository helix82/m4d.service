package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.synapi.enums.StatusTypeEnum;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name="mrpa_registry")
@Transactional
// Реестр МЧД
public class MrpaRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Column(name = "guid", columnDefinition = "VARCHAR(255)")
    @Schema(example = "6F9619FF-8B86-D011-B42D-00CF4FC964FF", required = false)
    @JsonProperty("guid")
    private String guid;

    @Column(name = "internal_number", columnDefinition = "VARCHAR(255)")
    @Schema(example = "1", required = false)
    @JsonProperty("internalNumber")
    private String internalNumber;

    @Column(name = "organisation_id")
    @Schema(example = "1", required = false)
    @JsonProperty("organisationId")
    private Long organisationId;

    @Column(name = "principal_id")
    @Schema(example = "1", required = false)
    @JsonProperty("principalId")
    private Long principalId;

    @Column(name = "representative_id")
    @Schema(example = "1", required = false)
    @JsonProperty("representativeId")
    private Long representativeId;

    @Column(name = "start_date")
    @Schema(example = "", required = false)
    @JsonProperty("startDate")
    private String startDate;

    @Column(name = "end_date")
    @Schema(example = "", required = false)
    @JsonProperty("endDate")
    private String endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(255)")
    @Schema(example = "EXPIRED", required = false)
    @JsonProperty("status")
    private StatusTypeEnum status;

    @Column(name = "sig_path", columnDefinition = "TEXT")
    @Schema(example = "", required = false)
    @JsonProperty("sigPath")
    private String sigPath;

    @Column(name = "mrpa_xml_path", columnDefinition = "TEXT")
    @Schema(example = "", required = false)
    @JsonProperty("mrpaXmlPath")
    private String mrpaXmlPath;

    @Column(name = "recalled_xml_path", columnDefinition = "TEXT")
    @Schema(example = "", required = false)
    @JsonProperty("recalledXmlPath")
    private String recalledXmlPath;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "mrpa_registry_id")
    private Set<MrpaRegistryCredential> mrpaRegistryCredentials = new HashSet<>();

    public void setGuid() {
        this.guid = UUID.randomUUID().toString();
    }
}
