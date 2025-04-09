package su.opencode.digsigcik.m4d.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import su.opencode.digsigcik.audit.AllowAudit;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "m4d_credential")
public class Credential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    @AllowAudit
    private Long id;

    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("name")
    @AllowAudit
    private String name;

    @Column(name = "code", columnDefinition = "VARCHAR(255)")
    @Schema(example = "EMPOWERMENT")
    @JsonProperty("code")
    @AllowAudit
    private String code;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "credential_id")
    private Set<M4dCredential> mrpaRegistryCredentials = new HashSet<>();
}
