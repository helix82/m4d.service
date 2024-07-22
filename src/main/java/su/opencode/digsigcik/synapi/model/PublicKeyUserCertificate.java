package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import su.opencode.digsigcik.synapi.enums.TypeUserCertificateEnum;

import javax.persistence.*;

@Data
@Entity
@Table(name = "user_certificate")
// Сертификаты пользователя
public class PublicKeyUserCertificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Column(name = "login", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("login")
    private String login;

    @Column(name = "imprint", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("imprint")
    private String imprint;

    @Column(name = "key_id", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("keyId")
    private String keyId;

    @Column(name = "serial_number", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("serialNumber")
    private String serialNumber;

    @Column(name = "type", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("type")
    private TypeUserCertificateEnum type;

    @Column(name = "fio", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("fio")
    private String fio;

    @Column(name = "inn", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("inn")
    private String inn;

    @Column(name = "organization", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("organization")
    private String organization;

    @Column(name = "issuer_who", columnDefinition = "TEXT")
    @Schema(example = "")
    @JsonProperty("issuerWho")
    private String issuerWho;

    @Column(name = "date_start", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("dateStart")
    private String dateStart;

    @Column(name = "date_end", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("dateEnd")
    private String dateEnd;

    @Column(name = "date_start_private_key", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("dateStartPrivateKey")
    private String dateStartPrivateKey;

    @Column(name = "date_end_private_key", columnDefinition = "VARCHAR(255)")
    @Schema(example = "")
    @JsonProperty("dateEndPrivateKey")
    private String dateEndPrivateKey;

    @Lob
    @Column(name = "certificate", columnDefinition = "bytea")
    @Schema(example = "")
    @JsonProperty("certificate")
    private String certificate;

    @Column(name = "original_filename")
    @Schema(example = "")
    @JsonProperty("originalFilename")
    private String originalFilename;

    @Column(name = "deleted", columnDefinition = "BOOLEAN")
    @Schema(example = "")
    @JsonProperty("deleted")
    private boolean deleted;

    @Column(name = "statement_id")
    @Schema(example = "")
    @JsonProperty("statementId")
    private Long statementId;
}
