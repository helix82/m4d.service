package su.opencode.digsigcik.synapi.model;

import lombok.*;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Table(name="mrpa_registry")
// Реестр МЧД
public class MrpaRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String guid;
    private String internalNumber;
    @ManyToOne
    @JoinColumn(name = "organisation_id")
    private Organisation organisation_name;
    @ManyToOne
    @JoinColumn(name = "principal_id")
    private Principal principal_name;
    @ManyToOne
    @JoinColumn(name = "representative_id")
    private Representative representative_name;
    private DateTime startDate;
    private DateTime endDate;
    private String status;
    private String ftxRegistration;
    private String sigPath;
    private String mrpaXmlPath;
    private String recalledXmlPath;
    private String credentials;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "mrpa_registry_credential",
            joinColumns = { @JoinColumn(name = "mrpa_registry_id")},
            inverseJoinColumns = { @JoinColumn(name = "credential_id")}
    )
    private Set<Credential> credentialSet  = new HashSet<>();
}
