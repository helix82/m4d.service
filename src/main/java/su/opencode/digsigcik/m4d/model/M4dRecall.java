package su.opencode.digsigcik.m4d.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import su.opencode.digsigcik.audit.AllowAudit;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "m4d_revoke")
public class M4dRecall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    @AllowAudit
    private Long id;

    @Column(name = "status")
    @Schema(example = "")
    @JsonProperty("status")
    @AllowAudit
    private String status;

    @Column(name = "error_detail")
    @Schema(example = "")
    @JsonProperty("errorDetail")
    @AllowAudit
    private String errorDetail;

    @Column(name = "create_date")
    @Schema(example = "")
    @JsonProperty("createDate")
    @AllowAudit
    private LocalDateTime createDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "xml_file_id", referencedColumnName = "id")
    @AllowAudit
    private M4dFile xmlFile;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "sig_file_id", referencedColumnName = "id")
    @AllowAudit
    private M4dFile sigFile;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "m4d_registry_id", referencedColumnName = "id")
    private M4d m4d;
}
