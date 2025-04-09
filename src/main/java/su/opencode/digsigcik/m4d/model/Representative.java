package su.opencode.digsigcik.m4d.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import su.opencode.digsigcik.audit.AllowAudit;

import javax.persistence.*;
import java.time.LocalDateTime;
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
    @AllowAudit
    private Long id;

    @Schema(example = "1234567890", required = true)
    @JsonProperty("inn")
    @AllowAudit
    private String inn;

    @Schema(example = "123-456-789 11")
    @JsonProperty("snils")
    @AllowAudit
    private String snils;

    @Column(name = "birth_date")
    @Schema(example = "")
    @JsonProperty("birthDate")
    @AllowAudit
    private LocalDateTime birthDate;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "representative_id", updatable = false)
    private Set<M4d> mrpaRegistryes = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "representative_id", updatable = false)
    private Set<RepresentativeDetail> representativeDetails = new HashSet<>();
}
