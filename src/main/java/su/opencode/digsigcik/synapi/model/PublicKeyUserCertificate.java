package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.synapi.enums.TypeUserCertificateEnum;

import javax.persistence.*;

@Data
@Entity
@Table(name="user_certificate")
@Transactional
// Сертификаты пользователя
public class PublicKeyUserCertificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Column(name = "login", columnDefinition = "VARCHAR(255)")
    @Schema(example = "", required = false)
    @JsonProperty("login")
    private String login;

    @Column(name = "key_id", columnDefinition = "VARCHAR(255)")
    @Schema(example = "", required = false)
    @JsonProperty("keyId")
    private String keyId;

    @Column(name = "serial_number", columnDefinition = "VARCHAR(255)")
    @Schema(example = "", required = false)
    @JsonProperty("serialNumber")
    private String serialNumber;

    @Column(name = "type", columnDefinition = "VARCHAR(255)")
    @Schema(example = "", required = false)
    @JsonProperty("type")
    private TypeUserCertificateEnum type;

    @Column(name = "fio", columnDefinition = "VARCHAR(255)")
    @Schema(example = "", required = false)
    @JsonProperty("fio")
    private String fio;

    @Column(name = "inn", columnDefinition = "VARCHAR(255)")
    @Schema(example = "", required = false)
    @JsonProperty("inn")
    private String inn;

    @Column(name = "date_start", columnDefinition = "VARCHAR(255)")
    @Schema(example = "", required = false)
    @JsonProperty("dateStart")
    private String dateStart;

    @Column(name = "date_end", columnDefinition = "VARCHAR(255)")
    @Schema(example = "", required = false)
    @JsonProperty("dateEnd")
    private String dateEnd;

    @Column(name = "issued_who", columnDefinition = "VARCHAR(255)")
    @Schema(example = "", required = false)
    @JsonProperty("issuedWho")
    private String issuedWho;

    @Lob
    @Column(name = "certificate", columnDefinition = "bytea")
    @Schema(example = "", required = false)
    @JsonProperty("certificate")
    private byte[] certificate;
}
