package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(name = "ИД представителя", example = "1", required = true)
    @JsonProperty("id")
    private Long id;
    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    @Schema(name = "Имя представителя", example = "", required = false)
    @JsonProperty("name")
    private String name;
    @Column(name = "surname", columnDefinition = "VARCHAR(255)")
    @Schema(name = "Фамилия представителя", example = "", required = false)
    @JsonProperty("surname")
    private String surname;
    @Column(name = "patronymic", columnDefinition = "VARCHAR(255)")
    @Schema(name = "Отчество представителя", example = "", required = false)
    @JsonProperty("patronymic")
    private String patronymic;
    @Schema(name = "ИНН представителя", example = "1234567890", required = true)
    @JsonProperty("inn")
    private String inn;
    @Schema(name = "СНИЛС представителя", example = "123-456-789 11", required = false)
    @JsonProperty("snils")
    private String snils;
    @Column(name = "birth_date")
    @Schema(name = "Дата рождения представителя", example = "", required = false)
    @JsonProperty("birthDate")
    private String birthDate;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "representative_id")
    private Set<MrpaRegistry> mrpaRegistryes = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "representative_id")
    private Set<RepresentativePassport> representativePassports = new HashSet<>();

}
