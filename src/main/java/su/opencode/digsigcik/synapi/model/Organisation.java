package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "organisation")
// Организация
public class Organisation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("name")
    private String name;

    @Schema(example = "1234567890", required = true)
    @JsonProperty("inn")
    private String inn;

    @Schema(example = "123456789")
    @JsonProperty("kpp")
    private String kpp;

    @Schema(example = "1234567890123")
    @JsonProperty("ogrn")
    private String ogrn;

    @Column(name = "legal_address", columnDefinition = "TEXT")
    @Schema(example = "")
    @JsonProperty("legalAddress")
    private String legalAddress;

    @Schema(example = "")
    @JsonProperty("regionCode")
    private String regionCode;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "organisation_id", updatable = false)
    private Set<MrpaRegistry> mrpaRegistryes = new HashSet<>();
}
