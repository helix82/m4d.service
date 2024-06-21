package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.dto.SesContainerFileDto;
import su.opencode.digsigcik.synapi.model.SesContainer;

public interface SesContainerService {
    SesContainer createSesContainer(SesContainerFileDto sesContainer);
    SesContainer findSesContainerById(Long id);
    boolean updateSesContainer(SesContainerFileDto sesContainer);
    void deleteSesContainer(Long id);
    String getSesContainerPathByUUID(String uuid);
}
