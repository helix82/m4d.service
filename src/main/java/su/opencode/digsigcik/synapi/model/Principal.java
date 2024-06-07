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
@Table(name="principal")
@Transactional
// Единоличный исполнительный орган
public class Principal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(name = "ИД доверителя", example = "1", required = true)
    @JsonProperty("id")
    private Long id;
    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    @Schema(name = "Имя доверителя", example = "", required = false)
    @JsonProperty("name")
    private String name;
    @Column(name = "surname", columnDefinition = "VARCHAR(255)")
    @Schema(name = "Фамилия доверителя", example = "", required = false)
    @JsonProperty("surname")
    private String surname;
    @Column(name = "patronymic", columnDefinition = "VARCHAR(255)")
    @Schema(name = "Отчество доверителя", example = "", required = false)
    @JsonProperty("patronymic")
    private String patronymic;
    @Schema(name = "ИНН доверителя", example = "1234567890", required = false)
    @JsonProperty("inn")
    private String inn;
    @Schema(name = "СНИЛС доверителя", example = "123-456-789 11", required = true)
    @JsonProperty("snils")
    private String snils;
    @Column(name = "job_title", columnDefinition = "VARCHAR(255)")
    @Schema(name = "Должность доверителя", example = "", required = false)
    @JsonProperty("jobTitle")
    private String jobTitle;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "principal_id")
    private Set<MrpaRegistry> mrpaRegistryes = new HashSet<>();
}
