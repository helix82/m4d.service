package su.opencode.digsigcik.synapi.model;

import lombok.Getter;
import lombok.Lombok;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Table(name="acmeaccount")
@Transactional
// AcmeAccount settings for acme
public class AcmeAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", columnDefinition = "VARCHAR(50)")
    private String name;

    @Column(name = "email", columnDefinition = "VARCHAR(200)")
    private String email;

    @Column(name = "external_key", columnDefinition = "VARCHAR(2000)")
    private String externalKey;

    @Column(name = "account_url", columnDefinition = "VARCHAR(2000)")
    private String accountUrl;

    @Column(name = "private_key", columnDefinition = "VARCHAR(2000)")
    private String accountPrivateKey;

    @Column(name = "deleted", columnDefinition = "BOOLEAN")
    private Boolean deleted;

    public AcmeAccount() {
    }

    public AcmeAccount(String name, String externalKey) {
        this.name = name;
        this.externalKey = externalKey;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public String getAccountPrivateKey() {
        return accountPrivateKey;
    }

    public String getAccountUrl() {
        return accountUrl;
    }

    public Boolean getDeleted() {
        return (deleted==null)?false:deleted;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setExternalKey(String externalKey) {
        this.externalKey = externalKey;
    }

    public void setAccountPrivateKey(String accountPrivateKey) {
        this.accountPrivateKey = accountPrivateKey;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public void setAccountUrl(String accountUrl) {
        this.accountUrl = accountUrl;
    }
}
