package su.opencode.digsigcik.m4d.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import su.opencode.digsigcik.m4d.audit.AllowAudit;
import su.opencode.digsigcik.m4d.enums.AuthorTypeEnum;

import javax.persistence.*;

@Data
@Entity
@Table(name = "m4d_revoke")
public class M4dRecall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "author", columnDefinition = "VARCHAR(255)")
    @Schema(example = "PRINCIPAL")
    @JsonProperty("author")
    private AuthorTypeEnum author;

    @Column(name = "argument", columnDefinition = "TEXT")
    @Schema(example = "")
    @JsonProperty("argument")
    private String argument;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "sig_file_id", referencedColumnName = "id")
    private M4dFile m4dFile;
}
