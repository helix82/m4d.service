package su.opencode.digsigcik.synapi.model;

import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name="organisation")
// Организация
public class Organisation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organisation_id")
    private Long id;
    private String name;
    private Integer inn;
    private Integer kpp;
    private Integer ogrn;
    private String jobTitle;

    @OneToMany(mappedBy = "organisation")
    private Set<MrpaRegistry> mrpaRegistry = new HashSet<>();
}
