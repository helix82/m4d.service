package su.opencode.digsigcik.synapi.model;

import lombok.Data;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

@Data
@Entity
@Table(name="representative_passport")
@Transactional
// Паспорт представителя
public class RepresentativePassport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "series_number", columnDefinition = "VARCHAR(50)")
    private String seriesNumber;
    @Column(name = "date_issue", columnDefinition = "VARCHAR(50)")
    private String dateIssue;
    @Column(name = "who_issue", columnDefinition = "VARCHAR(50)")
    private String whoIssue;
    @Column(name = "department_code", columnDefinition = "VARCHAR(50)")
    private String departmentCode;
}
