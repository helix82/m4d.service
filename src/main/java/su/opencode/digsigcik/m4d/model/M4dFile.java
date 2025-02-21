package su.opencode.digsigcik.m4d.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import su.opencode.digsigcik.m4d.enums.FileLogicalTypeEnum;
import su.opencode.digsigcik.m4d.enums.FileMimeTypeEnum;

import javax.persistence.*;
import java.util.Base64;
import java.util.regex.Pattern;

@Data
@Entity
@Table(name = "m4d_file")
public class M4dFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1", required = true)
    @JsonProperty("id")
    private Long id;

    @Column(name = "file_logical_type")
    @Schema(example = "")
    @JsonProperty("fileLogicalType")
    @Enumerated(EnumType.STRING)
    private FileLogicalTypeEnum fileLogicalType;

    @Column(name = "file_mime_type")
    @Schema(example = "")
    @JsonProperty("fileMimeType")
    @Enumerated(EnumType.STRING)
    private FileMimeTypeEnum fileMimeType;

    @Column(name = "filename")
    @Schema(example = "")
    @JsonProperty("filename")
    private String filename;

    @Column(name = "file_content", columnDefinition = "TEXT")
    @Schema(example = "")
    @JsonProperty("content")
    private String content;

    public void setContent(String content) {
        this.content = encodeToBase64(content);
    }

    public String getContent() {
        return (isBase64(this.content)) ? decodeFromBase64(this.content) : this.content;
    }

    private String encodeToBase64(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    private String decodeFromBase64(String base64Data) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
        return new String(decodedBytes);
    }

    private static boolean isBase64(String str) {
        final Pattern BASE64_PATTERN = Pattern.compile("^[A-Za-z0-9+/]+={0,2}$");
        if (!BASE64_PATTERN.matcher(str).matches()) {
            return false;
        }
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
