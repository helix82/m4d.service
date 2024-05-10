package su.opencode.digsigcik.synapi.model;

import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.synapi.enums.FtxRegistrationTypeEnum;
import su.opencode.digsigcik.synapi.enums.StatusTypeEnum;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Table(name="mrpa_registry")
@Transactional
// Реестр МЧД
public class MrpaRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "guid", columnDefinition = "VARCHAR(50)")
    private String guid;
    @Column(name = "internal_number", columnDefinition = "VARCHAR(50)")
    private String internalNumber;
    @Column(name = "organisation_id")
    private Long organisationId;
    @Column(name = "principal_id")
    private Long principalId;
    @Column(name = "representative_id")
    private Long representativeId;
    @Column(name = "start_date")
    private Timestamp startDate;
    @Column(name = "end_date")
    private Timestamp endDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(50)")
    private StatusTypeEnum status;
    @Enumerated(EnumType.STRING)
    @Column(name = "ftx_registration", columnDefinition = "VARCHAR(50)")
    private FtxRegistrationTypeEnum ftxRegistration;
    @Column(name = "sig_path", columnDefinition = "VARCHAR(50)")
    private String sigPath;
    @Column(name = "mrpa_xml_path", columnDefinition = "VARCHAR(50)")
    private String mrpaXmlPath;
    @Column(name = "recalled_xml_path", columnDefinition = "VARCHAR(50)")
    private String recalledXmlPath;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "mrpa_registry_credential",
            joinColumns = @JoinColumn(name = "mrpa_registry_id", referencedColumnName="id"),
            inverseJoinColumns = @JoinColumn(name = "credential_id", referencedColumnName="id")
    )
    private Set<Credential> credentials  = new HashSet<>();

    public MrpaRegistry() {

    }

    public MrpaRegistry(Long id, String guid, String internalNumber, Long organisationId, Long principalId, Long representativeId, Timestamp startDate, Timestamp endDate, StatusTypeEnum status, FtxRegistrationTypeEnum ftxRegistration, String sigPath, String mrpaXmlPath, String recalledXmlPath, Set<Credential> credentials) {
        this.id = id;
        this.guid = guid;
        this.internalNumber = internalNumber;
        this.organisationId = organisationId;
        this.principalId = principalId;
        this.representativeId = representativeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.ftxRegistration = ftxRegistration;
        this.sigPath = sigPath;
        this.mrpaXmlPath = mrpaXmlPath;
        this.recalledXmlPath = recalledXmlPath;
        this.credentials = credentials;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getInternalNumber() {
        return internalNumber;
    }

    public void setInternalNumber(String internalNumber) {
        this.internalNumber = internalNumber;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Long organisationId) {
        this.organisationId = organisationId;
    }

    public Long getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(Long principalId) {
        this.principalId = principalId;
    }

    public Long getRepresentativeId() {
        return representativeId;
    }

    public void setRepresentativeId(Long representativeId) {
        this.representativeId = representativeId;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public StatusTypeEnum getStatus() {
        return status;
    }

    public void setStatus(StatusTypeEnum status) {
        this.status = status;
    }

    public FtxRegistrationTypeEnum getFtxRegistration() {
        return ftxRegistration;
    }

    public void setFtxRegistration(FtxRegistrationTypeEnum ftxRegistration) {
        this.ftxRegistration = ftxRegistration;
    }

    public String getSigPath() {
        return sigPath;
    }

    public void setSigPath(String sigPath) {
        this.sigPath = sigPath;
    }

    public String getMrpaXmlPath() {
        return mrpaXmlPath;
    }

    public void setMrpaXmlPath(String mrpaXmlPath) {
        this.mrpaXmlPath = mrpaXmlPath;
    }

    public String getRecalledXmlPath() {
        return recalledXmlPath;
    }

    public void setRecalledXmlPath(String recalledXmlPath) {
        this.recalledXmlPath = recalledXmlPath;
    }

    public Set<Credential> getCredentials() {
        return credentials;
    }

    public void setCredentials(Set<Credential> credentials) {
        this.credentials = credentials;
    }
}
