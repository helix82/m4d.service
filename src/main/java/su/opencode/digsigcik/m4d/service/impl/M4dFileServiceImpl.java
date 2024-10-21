package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.m4d.exception.M4dException;
import su.opencode.digsigcik.m4d.model.M4dFile;
import su.opencode.digsigcik.m4d.model.Organisation;
import su.opencode.digsigcik.m4d.repository.M4dFileRepository;
import su.opencode.digsigcik.m4d.service.M4dFileService;

import javax.validation.constraints.NotNull;

@Service
@Primary
@RequiredArgsConstructor
public class M4dFileServiceImpl implements M4dFileService {
    private final M4dFileRepository repository;

    @Override
    public M4dFile findM4dFileById(@NotNull Long id) {
        M4dFile m4dFile = this.repository.findM4dFileById(id);
        return m4dFile;
    }

    @Override
    public void deleteM4dFile(@NotNull Long id) {
        this.repository.deleteById(id);
    }
}
