package su.opencode.digsigcik.m4d.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "m4d_revoke_confirm")
public class M4dRecallConfirm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Column(name = "registered_pep_date")
    @Schema(example = "")
    @JsonProperty("registeredPepDate")
    private LocalDateTime registeredPepDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "xml_file_registered_pep_id", referencedColumnName = "id")
    private M4dFile xmlFileRegisteredPep;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "sig_file_registered_pep_id", referencedColumnName = "id")
    private M4dFile sigFileRegisteredPep;

    @Column(name = "registered_rr_date")
    @Schema(example = "")
    @JsonProperty("registeredRrDate")
    private LocalDateTime registeredRrDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "xml_file_registered_rr_id", referencedColumnName = "id")
    private M4dFile xmlFileRegisteredRr;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "sig_file_registered_rr_id", referencedColumnName = "id")
    private M4dFile sigFileRegisteredRr;

    @OneToOne
    @JoinColumn(name = "m4d_revoke_id", referencedColumnName = "id")
    private M4d m4d;
}
