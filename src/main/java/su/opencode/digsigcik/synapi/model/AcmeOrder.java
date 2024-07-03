package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Getter
@Entity
@Table(name="acmeorder")
@Transactional
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
// AcmeCertificate settings for acme
public class AcmeOrder {
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

    @Column(name = "order_url", columnDefinition = "VARCHAR(2000)")
    private String orderUrl;

    @Column(name = "challenge_url", columnDefinition = "VARCHAR(2000)")
    private String challengeUrl;

    @Column(name = "challenge_token", columnDefinition = "VARCHAR(2000)")
    private String challengeToken;

    @Column(name = "challenge_content", columnDefinition = "VARCHAR(2000)")
    private String challengeContent;

    @Column(name = "deleted", columnDefinition = "BOOLEAN")
    private Boolean deleted;

    @Column(name = "notBefore", columnDefinition = "DATE")
    private Date notBefore;

    @Column(name = "notAfter", columnDefinition = "DATE")
    private Date notAfter;

    @Type(type = "jsonb")
    @Column(name = "data", columnDefinition = "jsonb")
    private Map<String, String[]> data = new HashMap<>();

    public AcmeOrder() {
    }

    public AcmeOrder(AcmeDomain domain) {
        this.domain = domain;
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

    public Boolean getDeleted() {
        return (deleted==null)?false:deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
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

    public Map<String, String[]> getData() {
        return data;
    }

    public void setData(Map<String, String[]> data) {
        this.data = data;
    }
}
