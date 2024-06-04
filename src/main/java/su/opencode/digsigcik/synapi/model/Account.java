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
@Table(name="account")
@Transactional
// Account settings for acme
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", columnDefinition = "VARCHAR(50)")
    private String name;

    @Column(name = "external_key", columnDefinition = "VARCHAR(2000)")
    private String externalKey;

    @Column(name = "account_url", columnDefinition = "VARCHAR(2000)")
    private String accountUrl;

    @Column(name = "private_key", columnDefinition = "VARCHAR(2000)")
    private String accountPrivateKey;

    @Column(name = "deleted", columnDefinition = "BOOLEAN")
    private Boolean deleted;

    public void setAccountUrl(String accountUrl) {
        this.accountUrl = accountUrl;
    }

    public String getAccountPrivateKey() {
        return accountPrivateKey;
    }
}
