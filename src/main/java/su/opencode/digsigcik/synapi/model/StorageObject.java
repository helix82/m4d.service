package su.opencode.digsigcik.synapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;

import javax.activation.MimetypesFileTypeMap;
import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Getter
@Entity
@Table(name = "storageobject")
// AcmeCertificate settings for acme
public class StorageObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "provider_id", columnDefinition = "LONG")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private AcmeProvider provider;

    @Column(name = "deleted", columnDefinition = "BOOLEAN")
    private Boolean deleted;

    @JsonIgnore
    @Column(name = "content", columnDefinition = "BYTEA")
    private byte[] content;

    @Type(type = "json")
    @Column(name = "data", columnDefinition = "json")
    private Map<String, Object> data = new HashMap<>();

    public StorageObject() {
    }

    public StorageObject(String fileName, byte[] content) {
        MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
        String mimeType = fileTypeMap.getContentType(fileName);
        HashMap<String, Object> data = new HashMap<>();
        data.put("fileName", fileName);
        data.put("mimeType", mimeType);
        data.put("fileSize", content.length);
        this.content = content;
        this.data = data;
    }

    public Long getId() {
        return id;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public Boolean getDeleted() {
        return (deleted == null) ? false : deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public AcmeProvider getProvider() {
        return provider;
    }

    public void setProvider(AcmeProvider provider) {
        this.provider = provider;
    }

}
