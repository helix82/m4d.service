package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
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

    @ManyToMany(cascade = CascadeType.ALL)
    @JsonIgnore
    /*@JoinTable(
            name = "mrpa_registry_credential",
            joinColumns = @JoinColumn(name = "credential_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "mrpa_registry_id", referencedColumnName = "id")
    )*/
    private Set<MrpaRegistry> mrpaRegistryes = new HashSet<>();

}
