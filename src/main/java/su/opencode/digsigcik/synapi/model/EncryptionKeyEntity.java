package su.opencode.digsigcik.synapi.model;

import lombok.Data;
import org.checkerframework.common.aliasing.qual.Unique;
import su.opencode.digsigcik.synapi.enums.SignatureAlgorithm;

import javax.persistence.*;

@Data
@Entity
@Table(name = "encryption_key")
public class EncryptionKeyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_projection_id")
    @Unique
    private UserProjection userProjection;

    @Column(name = "algorithm")
    private SignatureAlgorithm algorithm;
    @Lob
    @Column(name = "private_key", columnDefinition = "bytea")
    private byte[] privateKey;
    @Lob
    @Column(name = "public_key", columnDefinition = "bytea")
    private byte[] publicKey;

}