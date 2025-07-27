package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import su.opencode.digsigcik.audit.AuditLogEventEnum;
import su.opencode.digsigcik.audit.AuditLogThis;
import su.opencode.digsigcik.m4d.model.M4dRequestSearch;
import su.opencode.digsigcik.m4d.repository.M4dRequestRepository;
import su.opencode.digsigcik.m4d.service.M4dRequestService;

import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
@EnableTransactionManagement
public class M4dRequestServiceImpl implements M4dRequestService {
    private final M4dRequestRepository repository;

    @Override
    public List<M4dRequestSearch> findAllM4dRequestSearch() {
        return repository.findAll();
    }

    @Override
    public M4dRequestSearch findM4dRequestByGuidAndHash(@NotNull String guid, @NotNull String hash) {
        return repository.findByGuidAndHashXml(guid, hash);
    }

    @Override
    public M4dRequestSearch findM4dRequestByGuid(@NotNull String guid) {
        return repository.findByGuid(guid);
    }

    @Override
    public M4dRequestSearch saveM4dRequestSearch(M4dRequestSearch m4dRequestSearch) {
        return repository.save(m4dRequestSearch);
    }

    @Override
    public M4dRequestSearch updateM4dRequestSearch(M4dRequestSearch m4dRequestSearch) {
        return repository.save(m4dRequestSearch);
    }

    @Override
    //TODO: @helix82 - Использование Aspect-Oriented Programming (AOP) для передачи в логи guid?
    @AuditLogThis(event = AuditLogEventEnum.M4D_REQUEST_SEARCH_FIND_RR_DELETE, deleteByIdRepository = M4dRequestRepository.class)
    public void deleteM4dRequestSearch(@NotNull String guid) {
        M4dRequestSearch requestSearch = repository.findByGuid(guid);
        if (requestSearch != null) {
            repository.deleteById(requestSearch.getId());
        }
    }
}
