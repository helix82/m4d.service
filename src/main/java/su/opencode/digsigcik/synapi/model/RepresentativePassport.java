package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("id")
    private Long id;
    @Column(name = "series_number", columnDefinition = "VARCHAR(255)")
    @JsonProperty("seriesNumber")
    private String seriesNumber;
    @Column(name = "date_issue", columnDefinition = "VARCHAR(255)")
    @JsonProperty("dateIssue")
    private String dateIssue;
    @Column(name = "who_issue", columnDefinition = "TEXT")
    @JsonProperty("whoIssue")
    private String whoIssue;
    @Column(name = "department_code", columnDefinition = "VARCHAR(255)")
    @JsonProperty("departmentCode")
    private String departmentCode;
    @Column(name = "representative_id")
    @JsonProperty("representativeId")
    private Long representativeId;
}
