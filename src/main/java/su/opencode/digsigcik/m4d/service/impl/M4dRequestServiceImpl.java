package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import su.opencode.digsigcik.m4d.model.M4dRequestSearch;
import su.opencode.digsigcik.m4d.repository.M4dRequestRepository;
import su.opencode.digsigcik.m4d.service.M4dRequestService;

import javax.validation.constraints.NotNull;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
@EnableTransactionManagement
public class M4dRequestServiceImpl implements M4dRequestService {
    private final M4dRequestRepository repository;

    public M4dRequestSearch findM4dRequestByGuidAndHash(@NotNull String guid, @NotNull String hash) {
        return repository.findByGuidAndAndHashXml(guid, hash);
    }
}
