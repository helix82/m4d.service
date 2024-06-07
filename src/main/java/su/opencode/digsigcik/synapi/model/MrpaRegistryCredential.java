package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("id")
    private Long id;
    @Column(name = "mrpa_registry_id")
    @JsonProperty("mrpaRegistryId")
    private Long mrpaRegistryId;
    @Column(name = "credential_id")
    @JsonProperty("credentialId")
    private Long credentialId;

}
