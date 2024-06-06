package su.opencode.digsigcik.synapi.model;

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
    private Long id;
    @Column(name = "name", columnDefinition = "VARCHAR(50)")
    private String name;
    @Column(name = "code", columnDefinition = "VARCHAR(50)")
    private String code;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "credential_id")
    private Set<MrpaRegistryCredential> mrpaRegistryCredentials = new HashSet<>();
}
