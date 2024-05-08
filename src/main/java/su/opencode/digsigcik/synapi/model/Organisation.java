package su.opencode.digsigcik.synapi.model;

import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

@Getter
@Entity
@Table(name="organisation")
@Transactional
// Организация
public class Organisation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", columnDefinition = "VARCHAR(50)")
    private String name;
    private Integer inn;
    private Integer kpp;
    private Integer ogrn;
    @Column(name = "job_title", columnDefinition = "VARCHAR(50)")
    private String jobTitle;
}
