package su.opencode.digsigcik.synapi.model;

import lombok.Data;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
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
    @Column(name = "surname", columnDefinition = "VARCHAR(50)")
    private String surname;
    @Column(name = "patronymic", columnDefinition = "VARCHAR(50)")
    private String patronymic;
    private String inn;
    private String snils;
    @Column(name = "birth_date")
    private String birthDate;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "representative_id")
    private Set<MrpaRegistry> mrpaRegistryes = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "representative_id")
    private Set<RepresentativePassport> representativePassports = new HashSet<>();

}
