package su.opencode.digsigcik.synapi.model;

import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Table(name="organisation")
@Transactional
// Организация
public class Organisation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", columnDefinition = "VARCHAR(100)")
    private String name;
    private Long inn;
    private Long kpp;
    private Long ogrn;
    @Column(name = "legal_address", columnDefinition = "TEXT")
    private String legalAddress;
    private Long regionCode;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "organisation_id")
    private Set<MrpaRegistry> mrpaRegistryes = new HashSet<>();
}
