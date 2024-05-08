package su.opencode.digsigcik.synapi.model;

import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Entity
@Table(name="principal")
@Transactional
// Доверитель
public class Principal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", columnDefinition = "VARCHAR(50)")
    private String name;
    private Integer inn;
    private Integer kpp;
    private Integer snils;
    @Column(name = "birth_date")
    private Timestamp birthDate;
}
