package su.opencode.digsigcik.m4d.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import su.opencode.digsigcik.audit.AllowAudit;

import java.io.File;

@Data
public class UnpackedZipDto {
    @JsonProperty("jsonFile")
    @AllowAudit
    private File jsonFile;

    @JsonProperty("xmlFileM4d")
    @AllowAudit
    private File xmlFileM4d;

    @JsonProperty("xmlFileM4dRevoke")
    @AllowAudit
    private File xmlFileM4dRevoke;

    @JsonProperty("xmlFileKvitM4d")
    @AllowAudit
    private File xmlFileKvitM4d;

    @JsonProperty("xmlFileKvitM4dRevoke")
    @AllowAudit
    private File xmlFileKvitM4dRevoke;

    @JsonProperty("sigFileM4d")
    @AllowAudit
    private File sigFileM4d;

    @JsonProperty("sigFileM4dRevoke")
    @AllowAudit
    private File sigFileM4dRevoke;

    @JsonProperty("sigFileKvitM4d")
    @AllowAudit
    private File sigFileKvitM4d;

    @JsonProperty("sigFileKvitM4dRevoke")
    @AllowAudit
    private File sigFileKvitM4dRevoke;

    @JsonProperty("zipFile")
    @AllowAudit
    private File zipFile;
}