package su.opencode.digsigcik.m4d.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import su.opencode.digsigcik.audit.AllowAudit;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "m4d_principal")
public class Principal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    @AllowAudit
    private Long id;

    @Schema(example = "1234567890")
    @JsonProperty("inn")
    @AllowAudit
    private String inn;

    @Schema(example = "123-456-789 11", required = true)
    @JsonProperty("snils")
    @AllowAudit
    private String snils;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "principal_id", updatable = false)
    private Set<M4d> mrpaRegistryes = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "principal_id", updatable = false)
    private Set<PrincipalDetail> principalDetails = new HashSet<>();
}
