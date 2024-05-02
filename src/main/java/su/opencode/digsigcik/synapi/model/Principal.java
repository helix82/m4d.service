package su.opencode.digsigcik.synapi.model;

import lombok.Data;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name="principal")
// Доверитель
public class Principal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "principal_id")
    private Long id;
    private String name;
    private Integer inn;
    private Integer kpp;
    private Integer snils;
    private DateTime birthDate;

    @OneToMany(mappedBy = "principal")
    private Set<MrpaRegistry> mrpaRegistry = new HashSet<>();
}
