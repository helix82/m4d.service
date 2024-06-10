package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;
import su.opencode.digsigcik.synapi.repository.MrpaRegistryRepository;
import su.opencode.digsigcik.synapi.repository.OrganisationRepository;
import su.opencode.digsigcik.synapi.repository.PrincipalRepository;
import su.opencode.digsigcik.synapi.repository.RepresentativeRepository;

import javax.validation.constraints.NotNull;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constant.NOT_FOUND;

@Service
@Primary
@RequiredArgsConstructor
public class MrpaRegistryServiceImpl implements MrpaRegistryService {
    private final MrpaRegistryRepository mrpaRegistryRepository;
    private final OrganisationRepository organisationRepository;
    private final PrincipalRepository principalRepository;
    private final RepresentativeRepository representativeRepository;

    @Override
    public List<MrpaRegistryWithFioDTO> findAllMrpaRegistry() {
        return this.mrpaRegistryRepository.findAllMrpaRegistries();
    }

    @Override
    public MrpaRegistry saveMrpaRegistry(@NotNull MrpaRegistry mrpaRegistry) {
        throwExceptionIfRecordWithIdNotExist(mrpaRegistry, "create" );
        return this.mrpaRegistryRepository.save(mrpaRegistry);
    }

    @Override
    public List<MrpaRegistryWithFioDTO> findByMrpaRegistryId(@NotNull Long id) {
        List<MrpaRegistryWithFioDTO> mrpaRegistry = this.mrpaRegistryRepository.findMrpaRegistryById(id);
        if (mrpaRegistry.isEmpty()) {
            throw new MchdException(String.format("Mrpa registry %s", NOT_FOUND));
        }
        return mrpaRegistry;
    }

    @Override
    public List<MrpaRegistryWithFioDTO> findAllByMrpaRegistryUserId(Long userId) {
        return this.mrpaRegistryRepository.findAllMrpaRegistriesByUserId(userId);
    }

    @Override
    public List<MrpaRegistryFullDTO> findByMrpaRegistryUserId(@NotNull Long userId) {
        List<MrpaRegistryFullDTO> mrpaRegistryFullDto = this.mrpaRegistryRepository.findMrpaRegistriesByUserId(userId);
        if (mrpaRegistryFullDto.isEmpty()) {
            throw new MchdException(String.format("Mrpa registry full dto list %s", NOT_FOUND));
        }
        return mrpaRegistryFullDto;
    }

    @Override
    public MrpaRegistry updateMrpaRegistry(@NotNull MrpaRegistry mrpaRegistry) {
        throwExceptionIfRecordWithIdNotExist(mrpaRegistry, "update" );
        Long mrpaRegistryId = mrpaRegistry.getId();

        if (mrpaRegistryId == null) {
            throw new MchdException("Can`t update mrpa registry because id null!" );
        }

        List<MrpaRegistryWithFioDTO> existMrpaRegistry = this.mrpaRegistryRepository.findMrpaRegistryById(mrpaRegistryId);
        if (existMrpaRegistry.isEmpty()) {
            throw new MchdException(String.format("Can`t update. Mrpa registry with id=%d %s", mrpaRegistryId, NOT_FOUND));
        }

        return this.mrpaRegistryRepository.save(mrpaRegistry);
    }

    @Override
    public void deleteMrpaRegistry(@NotNull Long id) {
        List<MrpaRegistryWithFioDTO> mrpaRegistry = this.mrpaRegistryRepository.findMrpaRegistryById(id);
        if (mrpaRegistry.isEmpty()) {
            throw new MchdException(String.format("Can`t delete mrpa registry because mrpa registry %s", NOT_FOUND));
        }
        this.mrpaRegistryRepository.deleteById(id);
    }

    @Override
    public List<MrpaRegistryWithFioDTO> findByMrpaRegistryPrincipalInn(String inn) {
        return this.mrpaRegistryRepository.findMrpaRegistriesByPrincipalInn(inn);
    }

    private void throwExceptionIfRecordWithIdNotExist(MrpaRegistry mrpaRegistry, String operationType) {
        Long organisationId = mrpaRegistry.getOrganisationId();
        Long principalId = mrpaRegistry.getPrincipalId();
        Long representativeId = mrpaRegistry.getRepresentativeId();

        String errorMessage = "Can`t %s mrpa registry because %s with id=%d does not exist!";
        if (organisationRepository.findOrganisationById(organisationId) == null) {
            throw new MchdException(String.format(errorMessage, operationType, "organisation", organisationId));
        }
        if (principalRepository.findPrincipalById(principalId) == null) {
            throw new MchdException(String.format(errorMessage, operationType, "principal", principalId));
        }
        if (representativeRepository.findRepresentativeById(representativeId) == null) {
            throw new MchdException(String.format(errorMessage, operationType, "representative", representativeId));
        }
    }
}

