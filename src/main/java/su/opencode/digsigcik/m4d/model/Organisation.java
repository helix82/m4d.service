package su.opencode.digsigcik.m4d.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "m4d_organisation")
public class Organisation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Column(name = "name")
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
}
