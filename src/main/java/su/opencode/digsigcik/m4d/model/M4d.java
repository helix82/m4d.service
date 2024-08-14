package su.opencode.digsigcik.m4d.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import su.opencode.digsigcik.m4d.enums.StatusTypeEnum;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "mrpa_registry")
// Реестр МЧД
public class M4d {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Column(name = "guid", columnDefinition = "VARCHAR(255)")
    @Schema(example = "6F9619FF-8B86-D011-B42D-00CF4FC964FF")
    @JsonProperty("guid")
    private String guid;

    @Column(name = "internal_number", columnDefinition = "VARCHAR(255)")
    @Schema(example = "1")
    @JsonProperty("internalNumber")
    private String internalNumber;

    @Column(name = "organisation_id")
    @Schema(example = "1")
    @JsonProperty("organisationId")
    private Long organisationId;

    @Column(name = "principal_id")
    @Schema(example = "1")
    @JsonProperty("principalId")
    private Long principalId;

    @Column(name = "representative_id")
    @Schema(example = "1")
    @JsonProperty("representativeId")
    private Long representativeId;

    @Column(name = "start_date")
    @Schema(example = "")
    @JsonProperty("startDate")
    private String startDate;

    @Column(name = "end_date")
    @Schema(example = "")
    @JsonProperty("endDate")
    private String endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(255)")
    @Schema(example = "EXPIRED")
    @JsonProperty("status")
    private StatusTypeEnum status;

    @Column(name = "sig_path", columnDefinition = "TEXT")
    @Schema(example = "")
    @JsonProperty("sigPath")
    private String sigPath;

    @Column(name = "create_date")
    @Schema(example = "")
    @JsonProperty("createDate")
    private String createDate;

    @Column(name = "mrpa_xml_filename")
    @Schema(example = "")
    @JsonProperty("mrpaXmlFilename")
    private String mrpaXmlFilename;

    @Column(name = "recalled_xml_path", columnDefinition = "TEXT")
    @Schema(example = "")
    @JsonProperty("recalledXmlPath")
    private String recalledXmlPath;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "mrpa_registry_id")
    private Set<M4dCredential> m4dCredentials = new HashSet<>();

    public void setGuid() {
        this.guid = UUID.randomUUID()
                .toString();
    }
}
