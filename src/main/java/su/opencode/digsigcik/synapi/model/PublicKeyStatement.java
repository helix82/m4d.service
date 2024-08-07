package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import su.opencode.digsigcik.synapi.enums.StatusStatementEnum;
import su.opencode.digsigcik.synapi.enums.TypeStatementEnum;

import javax.persistence.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "statement")
// Заявления
public class PublicKeyStatement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Column(name = "login", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("login")
    private String login;

    @Column(name = "dt_create", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("dtCreate")
    private String dtCreate;

    @Column(name = "verification_center_id", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("verificationCenterId")
    private String verificationCenterId;

    @Column(name = "type", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("type")
    private TypeStatementEnum type;

    @Column(name = "status", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("status")
    private StatusStatementEnum status;
}
