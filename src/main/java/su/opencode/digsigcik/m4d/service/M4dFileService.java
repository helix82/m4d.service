package su.opencode.digsigcik.m4d.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.core.Authentication;
import su.opencode.digsigcik.m4d.enums.FileLogicalTypeEnum;
import su.opencode.digsigcik.m4d.enums.FileMimeTypeEnum;
import su.opencode.digsigcik.m4d.model.M4dFile;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface M4dFileService {
    M4dFile findM4dFileById(@NotNull Long id);
    void deleteM4dFile(@NotNull Long id);
    M4dFile saveM4dFile(@NotNull M4dFile m4dFile);
    M4dFile findM4dFileByContent(@NotNull String content);
    M4dFile fillM4dFile(FileMimeTypeEnum fileMimeTypeEnum, FileLogicalTypeEnum fileLogicalTypeEnum, String filename, byte[] content);
    ByteArrayResource createArchive(Authentication authentication, String filename, List<Long> values) throws IOException;
    File createArchiveToFile(Authentication authentication, String filename, List<Long> values) throws IOException;
}
