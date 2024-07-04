package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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

@Getter
@Entity
@Table(name="rootcertificate")
@Transactional
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
}
