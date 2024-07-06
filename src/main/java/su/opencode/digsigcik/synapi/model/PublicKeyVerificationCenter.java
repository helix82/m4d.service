package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name="verification_center")
@Transactional
// Удостоверяющие центры и шаблоны заявлений
public class PublicKeyVerificationCenter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    @Schema(example = "", required = false)
    @JsonProperty("name")
    private String name;

    @Lob
    @Column(name = "template_release", columnDefinition = "bytea")
    @Schema(example = "", required = false)
    @JsonProperty("templateRelease")
    private byte[] templateRelease;

    @Column(name = "content_type_release")
    @Schema(example = "", required = false)
    @JsonProperty("contentTypeRelease")
    private String contentTypeRelease;

    @Column(name = "original_name_release")
    @Schema(example = "", required = false)
    @JsonProperty("originalNameRelease")
    private String originalNameRelease;

    @Lob
    @Column(name = "template_recall", columnDefinition = "bytea")
    @Schema(example = "", required = false)
    @JsonProperty("templateRecall")
    private byte[] templateRecall;

    @Column(name = "content_type_recall")
    @Schema(example = "", required = false)
    @JsonProperty("contentTypeRecall")
    private String contentTypeRecall;

    @Column(name = "original_name_recall")
    @Schema(example = "", required = false)
    @JsonProperty("originalNameRecall")
    private String originalNameRecall;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "verification_center_id")
    private Set<PublicKeyStatement> publicKeyStatements = new HashSet<>();
}
