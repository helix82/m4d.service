package su.opencode.digsigcik.synapi.model;

import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Entity
@Table(name="acmeprovider")
@Transactional
// AcmeAccount settings for acme
public class AcmeProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", columnDefinition = "VARCHAR(50)")
    private String name;

    @Column(name = "directory_url", columnDefinition = "VARCHAR(400)")
    private String directoryUrl;


    @Column(name = "automated_release", columnDefinition = "BOOLEAN")
    private Boolean automatedRelease;

    @Column(name = "deleted", columnDefinition = "BOOLEAN")
    private Boolean deleted;

    public AcmeProvider() {
    }

    public AcmeProvider(String name, String directoryUrl) {
        this.name = name;
        this.directoryUrl = directoryUrl;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDirectoryUrl() {
        return directoryUrl;
    }

    public Boolean getAutomatedRelease() {
        return automatedRelease;
    }

    public Boolean getDeleted() {
        return (deleted==null)?false:deleted;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public void setDirectoryUrl(String directoryUrl) {
        this.directoryUrl = directoryUrl;
    }

    public void setAutomatedRelease(Boolean automatedRelease) {
        this.automatedRelease = automatedRelease;
    }
}
