package su.opencode.digsigcik.synapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import su.opencode.digsigcik.synapi.enums.HashAlgorithm;
import su.opencode.digsigcik.synapi.enums.SignatureAlgorithm;
import su.opencode.digsigcik.synapi.model.audit.DateAudit;

import javax.persistence.*;

@Builder
@Data
@Entity
@Table(name = "pincode")
@NoArgsConstructor
@AllArgsConstructor
public class PinCode extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_projection_id")
    private UserProjection userProjection;
    @Column(name = "algorithm")
    private HashAlgorithm algorithm;
    @Column(name = "pin_code_hash")
    @Schema(name = "Хэш пин-кода", example = "cd70b...24f45a")
    private String pinCodeHash;

}