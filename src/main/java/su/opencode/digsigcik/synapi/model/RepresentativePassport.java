package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "representative_passport")
// Паспорт представителя
public class RepresentativePassport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Column(name = "series_number", columnDefinition = "VARCHAR(255)")
    @Schema(example = "1234 123456", required = true)
    @JsonProperty("seriesNumber")
    private String seriesNumber;

    @Column(name = "date_issue", columnDefinition = "VARCHAR(255)")
    @Schema(example = "01.01.2000")
    @JsonProperty("dateIssue")
    private String dateIssue;

    @Column(name = "who_issue", columnDefinition = "TEXT")
    @Schema(example = "")
    @JsonProperty("whoIssue")
    private String whoIssue;

    @Column(name = "department_code", columnDefinition = "VARCHAR(255)")
    @Schema(example = "123 123")
    @JsonProperty("departmentCode")
    private String departmentCode;

    @Column(name = "representative_id")
    @Schema(example = "1")
    @JsonProperty("representativeId")
    private Long representativeId;
}
