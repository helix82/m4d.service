package su.opencode.digsigcik.synapi.model;

import lombok.*;
import org.joda.time.DateTime;

import javax.persistence.*;

@Getter
@Entity
@Table(name="mrpa_registry")
// Реестр МЧД
public class MrpaRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mrpa_registry_id")
    private Long id;
    private String guid;
    private String internalNumber;
    @ManyToOne
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;
    @ManyToOne
    @JoinColumn(name = "principal_id")
    private Principal principal;
    @ManyToOne
    @JoinColumn(name = "representative_id")
    private Representative representative;
    private DateTime startDate;
    private DateTime endDate;
    private String status;
    private String ftxRegistration;
    private String sigPath;
    private String mrpaXmlPath;
    private String recalledXmlPath;
    private String credentials;
}
