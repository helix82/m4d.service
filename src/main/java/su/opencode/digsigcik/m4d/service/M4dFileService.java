package su.opencode.digsigcik.m4d.service;

import su.opencode.digsigcik.m4d.model.M4dFile;

import javax.validation.constraints.NotNull;

public interface M4dFileService {
    M4dFile findM4dFileById(@NotNull Long id);
    void deleteM4dFile(@NotNull Long id);
    M4dFile saveM4dFile(@NotNull M4dFile m4dFile);
    M4dFile findM4dFileByContent(@NotNull String content);
}
