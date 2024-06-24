package su.opencode.digsigcik.synapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Builder
@Data
@Entity
@Table(name = "pincode")
@NoArgsConstructor
@AllArgsConstructor
public class PinCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private ESSUser user;

    @Column(name = "pin_code_hash", columnDefinition = "VARCHAR(64)")
    @Schema(name = "Хэш пин-кода", example = "cd70b...24f45a")
    private String pinCodeHash;

}