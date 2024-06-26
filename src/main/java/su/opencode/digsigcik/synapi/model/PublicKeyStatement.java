package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.synapi.enums.StatusStatementEnum;

import javax.persistence.*;

@Data
@Entity
@Table(name="statement")
@Transactional
// Заявления
public class PublicKeyStatement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Column(name = "login", columnDefinition = "VARCHAR(255)")
    @Schema(example = "", required = false)
    @JsonProperty("login")
    private String login;

    @Column(name = "dt_create", columnDefinition = "VARCHAR(255)")
    @Schema(example = "", required = false)
    @JsonProperty("dtCreate")
    private String dtCreate;

    @Column(name = "template_id", columnDefinition = "VARCHAR(255)")
    @Schema(example = "", required = false)
    @JsonProperty("templateId")
    private String templateId;

    @Column(name = "status", columnDefinition = "VARCHAR(255)")
    @Schema(example = "", required = false)
    @JsonProperty("status")
    private StatusStatementEnum status;
}
