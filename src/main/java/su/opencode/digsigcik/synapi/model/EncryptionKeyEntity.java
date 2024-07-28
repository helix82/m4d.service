package su.opencode.digsigcik.synapi.model;

import lombok.Data;
import org.checkerframework.common.aliasing.qual.Unique;
import su.opencode.digsigcik.synapi.enums.SimpleSignatureAlgorithm;

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
    private UserProjection userProjection;

    @Column(name = "algorithm")
    private SimpleSignatureAlgorithm algorithm;
    @Lob
    @Column(name = "private_key", columnDefinition = "bytea")
    private byte[] privateKey;
    @Lob
    @Column(name = "public_key", columnDefinition = "bytea")
    private byte[] publicKey;
    @Lob
    @Column(name = "cert", columnDefinition = "bytea")
    private byte[] cert;
}