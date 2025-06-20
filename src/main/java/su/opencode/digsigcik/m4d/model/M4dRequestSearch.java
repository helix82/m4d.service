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
@Table(name = "m4d_request_search")
public class M4dRequestSearch {
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

    @Column(name = "hash_xml", columnDefinition = "TEXT")
    @Schema(example = "")
    @JsonProperty("hashXml")
    @AllowAudit
    private String hashXml;

    @Column(name = "create_date")
    @Schema(example = "")
    @JsonProperty("createDate")
    @AllowAudit
    private LocalDateTime createDate;

    @Column(name = "modify_date")
    @Schema(example = "")
    @JsonProperty("modifyDate")
    @AllowAudit
    private LocalDateTime modifyDate;

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
}
