package su.opencode.digsigcik.m4d.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import su.opencode.digsigcik.audit.AllowAudit;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "m4d_representative_detail")
public class RepresentativeDetail {
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

    @Column(name = "series_number")
    @Schema(example = "1234 123456", required = true)
    @JsonProperty("seriesNumber")
    @AllowAudit
    private String seriesNumber;

    @Column(name = "date_issue")
    @Schema(example = "01.01.2000")
    @JsonProperty("dateIssue")
    @AllowAudit
    private LocalDateTime dateIssue;

    @Column(name = "who_issue")
    @Schema(example = "")
    @JsonProperty("whoIssue")
    @AllowAudit
    private String whoIssue;

    @Column(name = "department_code")
    @Schema(example = "123 123")
    @JsonProperty("departmentCode")
    @AllowAudit
    private String departmentCode;

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

    @Column(name = "representative_id")
    @Schema(example = "1")
    @JsonProperty("representativeId")
    @AllowAudit
    private Long representativeId;
}
