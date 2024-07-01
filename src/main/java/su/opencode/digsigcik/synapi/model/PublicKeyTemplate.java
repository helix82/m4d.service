package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.synapi.enums.TypeTemplateEnum;
import su.opencode.digsigcik.synapi.enums.VerificationCenterEnum;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name="template")
@Transactional
// Шаблоны заявлений
public class PublicKeyTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Column(name = "type", columnDefinition = "VARCHAR(255)")
    @Schema(example = "", required = false)
    @JsonProperty("type")
    private TypeTemplateEnum type;

    @Column(name = "verification_center", columnDefinition = "VARCHAR(255)")
    @Schema(example = "", required = false)
    @JsonProperty("verificationCenter")
    private VerificationCenterEnum verificationCenter;

    @Lob
    @Column(name = "template", columnDefinition = "BLOB")
    @Schema(example = "", required = false)
    @JsonProperty("template")
    private byte[] template;

    @Column(name = "content_type")
    @Schema(example = "", required = false)
    @JsonProperty("contentType")
    private String contentType;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "template_id")
    private Set<PublicKeyStatement> publicKeyStatements = new HashSet<>();
}
