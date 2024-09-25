package su.opencode.digsigcik.m4d.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "m4d_representative")
public class Representative {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Column(name = "name")
    @Schema(example = "")
    @JsonProperty("name")
    private String name;

    @Column(name = "surname")
    @Schema(example = "")
    @JsonProperty("surname")
    private String surname;

    @Column(name = "patronymic")
    @Schema(example = "")
    @JsonProperty("patronymic")
    private String patronymic;

    @Schema(example = "1234567890", required = true)
    @JsonProperty("inn")
    private String inn;

    @Schema(example = "123-456-789 11")
    @JsonProperty("snils")
    private String snils;

    @Column(name = "birth_date")
    @Schema(example = "")
    @JsonProperty("birthDate")
    private Timestamp birthDate;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "representative_id", updatable = false)
    private Set<M4d> mrpaRegistryes = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "representative_id", updatable = false)
    private Set<RepresentativePassport> representativePassports = new HashSet<>();
}
