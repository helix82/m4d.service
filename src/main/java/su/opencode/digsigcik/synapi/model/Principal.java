package su.opencode.digsigcik.synapi.model;

import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Table(name="principal")
@Transactional
// Единоличный исполнительный орган
public class Principal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", columnDefinition = "VARCHAR(50)")
    private String name;
    @Column(name = "surname", columnDefinition = "VARCHAR(50)")
    private String surname;
    @Column(name = "patronymic", columnDefinition = "VARCHAR(50)")
    private String patronymic;
    private Long inn;
    private Long snils;
    @Column(name = "job_title", columnDefinition = "VARCHAR(50)")
    private String jobTitle;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "principal_id")
    private Set<MrpaRegistry> mrpaRegistryes = new HashSet<>();
}
