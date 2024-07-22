package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "credential")
// Полномочия
public class Credential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("name")
    private String name;

    @Column(name = "code", columnDefinition = "VARCHAR(255)")
    @Schema(example = "EMPOWERMENT")
    @JsonProperty("code")
    private String code;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "credential_id")
    private Set<MrpaRegistryCredential> mrpaRegistryCredentials = new HashSet<>();
}
