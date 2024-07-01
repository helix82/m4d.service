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
import java.util.Date;

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

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "provider_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private AcmeProvider provider;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private AcmeOrder order;

    @Column(name = "certificate_url", columnDefinition = "VARCHAR(2000)")
    private String certificateUrl;

    @Column(name = "deleted", columnDefinition = "BOOLEAN")
    private Boolean deleted;

    @Column(name = "revoked", columnDefinition = "BOOLEAN")
    private Boolean revoked;

    @Column(name = "notBefore", columnDefinition = "DATE")
    private Date notBefore;

    @Column(name = "notAfter", columnDefinition = "DATE")
    private Date notAfter;

    @JsonIgnore
    @Column(name = "content", columnDefinition = "BYTEA")
    private byte[] content;

    public AcmeCertificate() {
    }

    public AcmeCertificate(AcmeDomain domain, String content) {
        this.domain = domain;
        this.content = content.getBytes();
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

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
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

    public String getCertificateUrl() {
        return certificateUrl;
    }

    public void setCertificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }

    public AcmeProvider getProvider() {
        return provider;
    }

    public void setProvider(AcmeProvider provider) {
        this.provider = provider;
    }

    public AcmeOrder getOrder() {
        return order;
    }

    public void setOrder(AcmeOrder order) {
        this.order = order;
    }

    public Date getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(Date notBefore) {
        this.notBefore = notBefore;
    }

    public Date getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(Date notAfter) {
        this.notAfter = notAfter;
    }
}
