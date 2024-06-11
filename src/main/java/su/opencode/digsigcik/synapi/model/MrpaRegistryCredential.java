package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

@Data
@Entity
@Table(name = "mrpa_registry_credential")
@Transactional
public class MrpaRegistryCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(name = "ИД связки полномочия с МЧД", example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Column(name = "mrpa_registry_id")
    @Schema(name = "ИД МЧД", example = "1", required = true)
    @JsonProperty("mrpaRegistryId")
    private Long mrpaRegistryId;

    @Column(name = "credential_id")
    @Schema(name = "ИД полномочия", example = "1", required = true)
    @JsonProperty("credentialId")
    private Long credentialId;

}
