package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("id")
    private Long id;
    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    @JsonProperty("name")
    private String name;
    @Column(name = "surname", columnDefinition = "VARCHAR(255)")
    @JsonProperty("surname")
    private String surname;
    @Column(name = "patronymic", columnDefinition = "VARCHAR(255)")
    @JsonProperty("patronymic")
    private String patronymic;
    @JsonProperty("inn")
    private String inn;
    @JsonProperty("snils")
    private String snils;
    @Column(name = "birth_date")
    @JsonProperty("birthDate")
    private String birthDate;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "representative_id")
    private Set<MrpaRegistry> mrpaRegistryes = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "representative_id")
    private Set<RepresentativePassport> representativePassports = new HashSet<>();

}
