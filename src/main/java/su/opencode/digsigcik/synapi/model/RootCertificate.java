package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

@Getter
@Entity
@Table(name="rootcertificate")
@Transactional
@TypeDef(name = "json", typeClass = JsonType.class)
// AcmeCertificate settings for acme
public class RootCertificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "provider_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private AcmeProvider provider;

    @Column(name = "deleted", columnDefinition = "BOOLEAN")
    private Boolean deleted;

    @Column(name = "revoked", columnDefinition = "BOOLEAN")
    private Boolean revoked;

    @JsonIgnore
    @Column(name = "content", columnDefinition = "VARCHAR(16000)")
    private String content;

    @Type(type = "json")
    @Column(name = "data", columnDefinition = "json")
    private Map<String, Object> data = new HashMap<>();

    public RootCertificate() {
    }

    public RootCertificate(AcmeProvider provider, String content) {
        this.provider = provider;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public AcmeProvider getProvider() {
        return provider;
    }

    public void setProvider(AcmeProvider provider) {
        this.provider = provider;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getDeleted() {
        return (deleted==null)?false:deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean getRevoked() {
        return (revoked==null)?false:revoked;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
