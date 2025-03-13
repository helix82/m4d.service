package su.opencode.digsigcik.m4d.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import su.opencode.digsigcik.audit.AllowAudit;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "m4d_request_search_detail")
public class M4dRequestSearchDetail {
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

    @Column(name = "status_date")
    @Schema(example = "")
    @JsonProperty("statusDate")
    @AllowAudit
    private LocalDateTime statusDate;

    @Column(name = "registration_date")
    @Schema(example = "")
    @JsonProperty("registrationDate")
    @AllowAudit
    private LocalDateTime registrationDate;

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

    @Column(name = "version")
    @Schema(example = "")
    @JsonProperty("version")
    private Long version;

    @Column(name = "is_active")
    @Schema(example = "")
    @JsonProperty("isActive")
    private boolean isActive;

    @Column(name = "request_search_id")
    @Schema(example = "1")
    @JsonProperty("requestSearchId")
    @AllowAudit
    private Long requestSearchId;
}
