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
    @Column(name = "series_number", columnDefinition = "VARCHAR(255)")
    private String seriesNumber;
    @Column(name = "date_issue", columnDefinition = "VARCHAR(255)")
    private String dateIssue;
    @Column(name = "who_issue", columnDefinition = "TEXT")
    private String whoIssue;
    @Column(name = "department_code", columnDefinition = "VARCHAR(255)")
    private String departmentCode;
    @Column(name = "representative_id")
    private Long representativeId;
}
