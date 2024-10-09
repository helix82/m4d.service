package su.opencode.digsigcik.m4d.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "m4d_representative_passport")
public class RepresentativePassport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Column(name = "series_number")
    @Schema(example = "1234 123456", required = true)
    @JsonProperty("seriesNumber")
    private String seriesNumber;

    @Column(name = "date_issue")
    @Schema(example = "01.01.2000")
    @JsonProperty("dateIssue")
    private LocalDateTime dateIssue;

    @Column(name = "who_issue")
    @Schema(example = "")
    @JsonProperty("whoIssue")
    private String whoIssue;

    @Column(name = "department_code")
    @Schema(example = "123 123")
    @JsonProperty("departmentCode")
    private String departmentCode;

    @Column(name = "representative_id")
    @Schema(example = "1")
    @JsonProperty("representativeId")
    private Long representativeId;
}
