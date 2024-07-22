package su.opencode.digsigcik.synapi.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.Getter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Getter
@Entity
@Table(name = "acmeprovider")
@TypeDef(name = "json", typeClass = JsonType.class)
// AcmeAccount settings for acme
public class AcmeProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", columnDefinition = "VARCHAR(50)")
    private String name;

    @Column(name = "description", columnDefinition = "VARCHAR(2000)")
    private String description;

    @Column(name = "domain", columnDefinition = "VARCHAR(400)")
    private String domain;

    @Column(name = "ip", columnDefinition = "VARCHAR(400)")
    private String ip;

    @Column(name = "directory_url", columnDefinition = "VARCHAR(400)")
    private String directoryUrl;


    @Column(name = "automated_release", columnDefinition = "BOOLEAN")
    private Boolean automatedRelease;

    @Column(name = "deleted", columnDefinition = "BOOLEAN")
    private Boolean deleted;

    @Type(type = "json")
    @Column(name = "data", columnDefinition = "json")
    private Map<String, Object> data = new HashMap<>();

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
        return (deleted == null) ? false : deleted;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

}
