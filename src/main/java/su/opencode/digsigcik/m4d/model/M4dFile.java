package su.opencode.digsigcik.m4d.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import su.opencode.digsigcik.m4d.enums.FileLogicalTypeEnum;
import su.opencode.digsigcik.m4d.enums.FileMimeTypeEnum;

import javax.persistence.*;

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

    @Column(name = "content", columnDefinition = "TEXT")
    @Schema(example = "")
    @JsonProperty("content")
    private String content;
}
