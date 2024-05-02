package su.opencode.digsigcik.synapi.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Entity
@Table(name="credential")
// Полномочия
public class Credential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credential_id")
    private Long id;
    private String name;
}
