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
@Table(name="credential")
@Transactional
// Полномочия
public class Credential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(name = "ИД полномочия", example = "1", required = true)
    @JsonProperty("id")
    private Long id;
    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    @Schema(name = "Наименование полномочия", example = "", required = false)
    @JsonProperty("name")
    private String name;
    @Column(name = "code", columnDefinition = "VARCHAR(255)")
    @Schema(name = "Код полномочия", example = "EMPOWERMENT", required = false)
    @JsonProperty("code")
    private String code;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "credential_id")
    private Set<MrpaRegistryCredential> mrpaRegistryCredentials = new HashSet<>();
}
