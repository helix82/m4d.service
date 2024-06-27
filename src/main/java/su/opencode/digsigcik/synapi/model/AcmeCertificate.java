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

    @Column(name = "challenge_url", columnDefinition = "VARCHAR(2000)")
    private String challengeUrl;

    @Column(name = "order_url", columnDefinition = "VARCHAR(2000)")
    private String orderUrl;

    @Column(name = "challenge_token", columnDefinition = "VARCHAR(2000)")
    private String challengeToken;

    @Column(name = "challenge_content", columnDefinition = "VARCHAR(2000)")
    private String challengeContent;

    @Column(name = "certificate_url", columnDefinition = "VARCHAR(2000)")
    private String certificateUrl;

    @Column(name = "deleted", columnDefinition = "BOOLEAN")
    private Boolean deleted;

    @Column(name = "revoked", columnDefinition = "BOOLEAN")
    private Boolean revoked;

    @JsonIgnore
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

    public String getChallengeUrl() {
        return challengeUrl;
    }

    public void setChallengeUrl(String challengeUrl) {
        this.challengeUrl = challengeUrl;
    }

    public String getChallengeToken() {
        return challengeToken;
    }

    public void setChallengeToken(String challengeToken) {
        this.challengeToken = challengeToken;
    }

    public String getChallengeContent() {
        return challengeContent;
    }

    public void setChallengeContent(String challengeContent) {
        this.challengeContent = challengeContent;
    }

    public String getOrderUrl() {
        return orderUrl;
    }

    public void setOrderUrl(String orderUrl) {
        this.orderUrl = orderUrl;
    }

    public AcmeProvider getProvider() {
        return provider;
    }

    public void setProvider(AcmeProvider provider) {
        this.provider = provider;
    }
}
