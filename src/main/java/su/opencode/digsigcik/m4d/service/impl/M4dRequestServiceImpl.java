package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import su.opencode.digsigcik.m4d.model.M4dRequestSearch;
import su.opencode.digsigcik.m4d.model.M4dRequestSearchDetail;
import su.opencode.digsigcik.m4d.repository.M4dRequestDetailRepository;
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
    private final M4dRequestDetailRepository detailRepository;

    public M4dRequestSearch findM4dRequestByGuidAndHash(@NotNull String guid, @NotNull String hash) {
        return repository.findByGuidAndAndHashXml(guid, hash);
    }

    public M4dRequestSearchDetail findM4dRequestDetailByM4dRequestId(@NotNull Long id) {
        return detailRepository.findByRequestSearchId(id);
    }

    public M4dRequestSearch saveM4dRequestSearch(M4dRequestSearch m4dRequestSearch) {
        return repository.save(m4dRequestSearch);
    }

    public M4dRequestSearchDetail saveM4dRequestSearchDetail(M4dRequestSearchDetail m4dRequestSearchDetail) {
        return detailRepository.save(m4dRequestSearchDetail);
    }

    public M4dRequestSearchDetail updateM4dRequestSearchDetail(M4dRequestSearchDetail m4dRequestSearchDetail) {
        return detailRepository.save(m4dRequestSearchDetail);
    }
}
