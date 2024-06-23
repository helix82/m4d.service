package su.opencode.digsigcik.synapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import su.opencode.digsigcik.synapi.enums.SignatureAlgorithm;

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

    @Column(name = "pin_code")
    private String pinCode;

}