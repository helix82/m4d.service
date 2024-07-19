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
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Entity
@Table(name="acmedomain")
@Transactional
// AcmDomain settings for acme
public class AcmeDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private AcmeAccount account;

    @Column(name = "domain_url", columnDefinition = "VARCHAR(2000)")
    private String domainUrl;

    @Column(name = "private_key", columnDefinition = "BYTEA")
    private byte[] domainPrivateKey;

    @Column(name = "deleted", columnDefinition = "BOOLEAN")
    private Boolean deleted;


    public AcmeDomain() {
    }

    public AcmeDomain(AcmeAccount account, String domainUrl) {
        this.account = account;
        this.domainUrl = domainUrl;
    }

    public Long getId() {
        return id;
    }

    public AcmeAccount getAccount() {
        return account;
    }

    public void setAccount(AcmeAccount account) {
        this.account = account;
    }

    public String getDomainUrl() {
        return domainUrl;
    }

    public void setDomainUrl(String domainUrl) {
        this.domainUrl = domainUrl;
    }

    public byte[] getDomainPrivateKey() {
        return domainPrivateKey;
    }

    public void setDomainPrivateKey(byte[] domainPrivateKey) {
        this.domainPrivateKey = domainPrivateKey;
    }

    public Boolean getDeleted() {
        return (deleted==null)?false:deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
