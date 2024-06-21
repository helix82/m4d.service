package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.dto.SesContainerFileDto;
import su.opencode.digsigcik.synapi.exception.SesException;
import su.opencode.digsigcik.synapi.model.SesContainer;
import su.opencode.digsigcik.synapi.repository.SesContainerRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SesContainerServiceImpl implements SesContainerService {
    private final SesContainerRepository sesContainerRepository;

    @Override
    public SesContainer createSesContainer(SesContainerFileDto sesContainerFileDto) {
        SesContainer sesContainer = mapDtoToSesContainer(sesContainerFileDto);
        return sesContainerRepository.save(sesContainer);
    }

    @Override
    public SesContainer findSesContainerById(Long id) {
        return this.sesContainerRepository.findSesContainerById(id);
    }

    @Override
    public boolean updateSesContainer(SesContainerFileDto sesContainerFileDto) {
        return createSesContainer(sesContainerFileDto) != null;
    }

    @Override
    public void deleteSesContainer(Long id) {
        this.sesContainerRepository.deleteById(id);
    }

    @Override
    public String getSesContainerPathByUUID(String uuid) {
        SesContainer sesContainer = this.sesContainerRepository.findSesContainerByUuid(uuid);
        String sesContainerPath = sesContainer.getSesContainerPath();
        if (sesContainerPath == null || sesContainerPath.isEmpty()) {
            throw new SesException("Ses container path is null!");
        }
        return sesContainerPath;
    }

    private SesContainer mapDtoToSesContainer(SesContainerFileDto sesContainerFileDto) {
        SesContainer sesContainer = new SesContainer();
        sesContainer.setUuid(sesContainerFileDto.getUuid().toString());
        sesContainer.setSesContainerPath(sesContainerFileDto.getSesContainerPath());
        return sesContainer;
    }
}
