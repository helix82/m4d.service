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
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Getter
@Entity
@Table(name="acmecertificate")
@Transactional
// AcmeCertificate settings for acme
public class AcmeCertificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "domain_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private AcmeDomain domain;

    @Column(name = "content", columnDefinition = "VARCHAR(16000)")
    private String content;

    public AcmeCertificate() {
    }

    public AcmeCertificate(AcmeDomain domain, String content) {
        this.domain = domain;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public AcmeDomain getDomain() {
        return domain;
    }

    public void setDomain(AcmeDomain domain) {
        this.domain = domain;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
