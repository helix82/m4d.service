package su.opencode.digsigcik.m4d.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import su.opencode.digsigcik.audit.AllowAudit;

import javax.persistence.*;

@Data
@Entity
@Table(name = "m4d_principal_detail")
public class PrincipalDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    @AllowAudit
    private Long id;

    @Column(name = "name")
    @Schema(example = "")
    @JsonProperty("name")
    @AllowAudit
    private String name;

    @Column(name = "surname")
    @Schema(example = "")
    @JsonProperty("surname")
    @AllowAudit
    private String surname;

    @Column(name = "patronymic")
    @Schema(example = "")
    @JsonProperty("patronymic")
    @AllowAudit
    private String patronymic;

    @Column(name = "job_title")
    @Schema(example = "")
    @JsonProperty("jobTitle")
    @AllowAudit
    private String jobTitle;

    @Column(name = "version")
    @Schema(example = "")
    @JsonProperty("version")
    @AllowAudit
    private Long version;

    @Column(name = "is_active")
    @Schema(example = "")
    @JsonProperty("isActive")
    @AllowAudit
    private boolean isActive;

    @Column(name = "principal_id")
    @Schema(example = "1")
    @JsonProperty("principalId")
    @AllowAudit
    private Long principalId;
}
