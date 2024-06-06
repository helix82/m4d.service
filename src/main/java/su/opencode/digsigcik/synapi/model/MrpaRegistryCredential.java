package su.opencode.digsigcik.synapi.model;

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
    private Long id;
    @Column(name = "mrpa_registry_id")
    private Long mrpaRegistryId;
    @Column(name = "credential_id")
    private Long credentialId;

}
