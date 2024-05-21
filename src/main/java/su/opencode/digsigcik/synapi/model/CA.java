package su.opencode.digsigcik.synapi.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "cert_center_name")
@Transactional
public class CA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", columnDefinition = "VARCHAR(50)")
    private String name;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "cert_center_name_id")
    private Set<Certificate> certificates = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "cert_center_name_id")
    private Set<RevokedCertificate> revokedSertificates = new HashSet<>();
}
