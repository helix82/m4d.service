package su.opencode.digsigcik.synapi.model;

import lombok.Data;
import su.opencode.digsigcik.synapi.enums.SignatureAlgorithm;

import javax.persistence.*;

@Data
@Entity
@Table(name = "encryption_key")
public class EncryptionKeyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private ESSUser user;

    @Column(name = "algorithm")
    private SignatureAlgorithm algorithm;

    @Column(name = "private_key", columnDefinition = "BLOB")
    private byte[] privateKey;

    @Column(name = "public_key", columnDefinition = "BLOB")
    private byte[] publicKey;

    @Column(name = "deleted")
    private boolean deleted;

    @Column(name = "disabled")
    private boolean disabled;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pincode_id")
    private PinCode pinCode;

}