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
@Table(name="organisation")
@Transactional
// Организация
public class Organisation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(name = "ИД организации", example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    @Schema(name = "Наименование организации", example = "", required = false)
    @JsonProperty("name")
    private String name;

    @Schema(name = "ИНН организации", example = "1234567890", required = true)
    @JsonProperty("inn")
    private String inn;

    @Schema(name = "КПП организации", example = "123456789", required = false)
    @JsonProperty("kpp")
    private String kpp;

    @Schema(name = "ОГРН организации", example = "1234567890123", required = false)
    @JsonProperty("ogrn")
    private String ogrn;

    @Column(name = "legal_address", columnDefinition = "TEXT")
    @Schema(name = "Юридический адрес организации", example = "", required = false)
    @JsonProperty("legalAddress")
    private String legalAddress;

    @Schema(name = "Код региона организации", example = "", required = false)
    @JsonProperty("regionCode")
    private String regionCode;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "organisation_id")
    private Set<MrpaRegistry> mrpaRegistryes = new HashSet<>();
}
