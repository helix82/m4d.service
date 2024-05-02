package su.opencode.digsigcik.synapi.model;

import lombok.*;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Table(name="representative")
// Представитель
public class Representative {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "representative_id")
    private Long id;
    private String name;
    private Integer inn;
    private Integer kpp;
    private Integer snils;
    private DateTime birthDate;

    @OneToMany(mappedBy = "representative")
    private Set<MrpaRegistry> mrpaRegistry = new HashSet<>();
}
