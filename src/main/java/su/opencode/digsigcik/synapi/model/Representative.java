package su.opencode.digsigcik.synapi.model;

import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Table(name="representative")
@Transactional
// Представитель
public class Representative {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", columnDefinition = "VARCHAR(50)")
    private String name;
    private Long inn;
    private Long kpp;
    private Long snils;
    @Column(name = "birth_date")
    private Timestamp birthDate;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "representative_id")
    private Set<MrpaRegistry> mrpaRegistryes = new HashSet<>();
}
