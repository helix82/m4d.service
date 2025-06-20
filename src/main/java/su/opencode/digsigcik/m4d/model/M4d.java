package su.opencode.digsigcik.m4d.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import su.opencode.digsigcik.audit.AllowAudit;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "m4d_registry")
// Реестр МЧД
public class M4d {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    @AllowAudit
    private Long id;

    @Column(name = "guid", columnDefinition = "VARCHAR(255)")
    @Schema(example = "6F9619FF-8B86-D011-B42D-00CF4FC964FF")
    @JsonProperty("guid")
    @AllowAudit
    private String guid;

    @Column(name = "organisation_inn", columnDefinition = "VARCHAR(10)")
    @Schema(example = "1")
    @JsonProperty("organisationInn")
    @AllowAudit
    private String organisationInn;

    @Column(name = "principal_id")
    @Schema(example = "1")
    @JsonProperty("principalId")
    @AllowAudit
    private Long principalId;

    @Column(name = "principal_detail_id")
    @Schema(example = "1")
    @JsonProperty("principalDetailId")
    @AllowAudit
    private Long principalDetailId;

    @Column(name = "representative_id")
    @Schema(example = "1")
    @JsonProperty("representativeId")
    @AllowAudit
    private Long representativeId;

    @Column(name = "representative_detail_id")
    @Schema(example = "1")
    @JsonProperty("representativeDetailId")
    @AllowAudit
    private Long representativeDetailId;

    @Column(name = "start_date")
    @Schema(example = "")
    @JsonProperty("startDate")
    @AllowAudit
    private LocalDateTime startDate;

    @Column(name = "end_date")
    @Schema(example = "")
    @JsonProperty("endDate")
    @AllowAudit
    private LocalDateTime endDate;

    @Column(name = "status")
    @Schema(example = "")
    @JsonProperty("status")
    @AllowAudit
    private String status;

    @Column(name = "status_in_rr")
    @Schema(example = "")
    @JsonProperty("statusInRr")
    @AllowAudit
    private String statusInRr;

    @Column(name = "error_in_rr_detail")
    @Schema(example = "")
    @JsonProperty("errorInRrDetail")
    @AllowAudit
    private String errorInRrDetail;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "sig_file_id", referencedColumnName = "id")
    @AllowAudit
    private M4dFile sigFile;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "xml_file_id", referencedColumnName = "id")
    @AllowAudit
    private M4dFile xmlFile;

    @Column(name = "create_date")
    @Schema(example = "")
    @JsonProperty("createDate")
    @AllowAudit
    private LocalDateTime createDate;

    @Column(name = "is_sub_trust")
    @Schema(example = "")
    @JsonProperty("isSubTrust")
    @AllowAudit
    private Boolean isSubTrust;

    public void setGuid() {
        this.guid = UUID.randomUUID()
                .toString();
    }
}
