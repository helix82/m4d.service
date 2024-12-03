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
@Table(name = "m4d_verify_status")
public class M4dVerifyStatus {
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

    @Column(name = "verify_date")
    @Schema(example = "")
    @JsonProperty("verifyDate")
    @AllowAudit
    private LocalDateTime verifyDate;

    @Column(name = "status", columnDefinition = "VARCHAR(255)")
    @Schema(example = "1")
    @JsonProperty("status")
    @AllowAudit
    private String status;
}
