package su.opencode.digsigcik.m4d.service.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.m4d.repository.M4dRecallConfirmRepository;
import su.opencode.digsigcik.m4d.service.ContainerService;
import su.opencode.digsigcik.m4d.service.M4dFileService;
import su.opencode.digsigcik.m4d.service.M4dRecallConfirmService;

@Service
@Primary
public class M4dRecallConfirmServiceImpl implements M4dRecallConfirmService {
    private final M4dRecallConfirmRepository m4dRecallConfirmRepository;
    private final M4dFileService m4dFileService;
    private final ContainerService containerService;

    public M4dRecallConfirmServiceImpl(M4dRecallConfirmRepository m4dRecallConfirmRepository, M4dFileService m4dFileService, ContainerService containerService) {
        this.m4dRecallConfirmRepository = m4dRecallConfirmRepository;
        this.m4dFileService = m4dFileService;
        this.containerService = containerService;
    }


}
