package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "principal")
// Единоличный исполнительный орган
public class Principal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("name")
    private String name;

    @Column(name = "surname", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("surname")
    private String surname;

    @Column(name = "patronymic", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("patronymic")
    private String patronymic;

    @Schema(example = "1234567890")
    @JsonProperty("inn")
    private String inn;

    @Schema(example = "123-456-789 11", required = true)
    @JsonProperty("snils")
    private String snils;

    @Column(name = "job_title", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("jobTitle")
    private String jobTitle;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "principal_id")
    private Set<MrpaRegistry> mrpaRegistryes = new HashSet<>();
}
