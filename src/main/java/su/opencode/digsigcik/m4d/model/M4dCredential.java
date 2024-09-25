package su.opencode.digsigcik.m4d.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "m4d_registry_m4d_credential")
public class M4dCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Column(name = "mrpa_registry_id")
    @Schema(example = "1", required = true)
    @JsonProperty("mrpaRegistryId")
    private Long mrpaRegistryId;

    @Column(name = "credential_id")
    @Schema(example = "1", required = true)
    @JsonProperty("credentialId")
    private Long credentialId;

}
