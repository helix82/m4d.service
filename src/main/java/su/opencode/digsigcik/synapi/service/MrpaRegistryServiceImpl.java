package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
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
        throwExceptionIfRecordWithIdNotExist(mrpaRegistry, "create");
        return this.mrpaRegistryRepository.save(mrpaRegistry);
    }

    @Override
    public MrpaRegistryWithFioDTO findByMrpaRegistryId(@NotNull Long id) {
        MrpaRegistryWithFioDTO mrpaRegistry = this.mrpaRegistryRepository.findMrpaRegistryById(id);
        if (mrpaRegistry == null) {
            throw new MchdException(String.format("Mrpa registry %s", NOT_FOUND));
        }
        return mrpaRegistry;
    }

    @Override
    public MrpaRegistry findById(@NotNull Long id) {
        return this.mrpaRegistryRepository.findMrpaById(id);
    }

    @Override
    public MrpaRegistry updateMrpaRegistry(@NotNull MrpaRegistry mrpaRegistry) {
        throwExceptionIfRecordWithIdNotExist(mrpaRegistry, "update");
        Long mrpaRegistryId = mrpaRegistry.getId();

        if (mrpaRegistryId == null) {
            throw new BadRequestException("Can`t update mrpa registry because id null!");
        }

        MrpaRegistryWithFioDTO existMrpaRegistry = this.mrpaRegistryRepository.findMrpaRegistryById(mrpaRegistryId);
        if (existMrpaRegistry == null) {
            throw new MchdException(String.format("Can`t update. Mrpa registry with id=%d %s", mrpaRegistryId, NOT_FOUND));
        }

        return this.mrpaRegistryRepository.save(mrpaRegistry);
    }

    @Override
    public void deleteMrpaRegistry(@NotNull Long id) {
        MrpaRegistryWithFioDTO mrpaRegistry = this.mrpaRegistryRepository.findMrpaRegistryById(id);
        if (mrpaRegistry == null) {
            throw new MchdException(String.format("Can`t delete mrpa registry because mrpa registry %s", NOT_FOUND));
        }
        this.mrpaRegistryRepository.deleteById(id);
    }

    @Override
    public MrpaRegistryWithFioDTO findByMrpaRegistryPrincipalInn(String inn) {
        return this.mrpaRegistryRepository.findMrpaRegistriesByPrincipalInn(inn);
    }

    private void throwExceptionIfRecordWithIdNotExist(MrpaRegistry mrpaRegistry, String operationType) {
        Long organisationId = mrpaRegistry.getOrganisationId();
        Long principalId = mrpaRegistry.getPrincipalId();
        Long representativeId = mrpaRegistry.getRepresentativeId();

        String errorMessage = "Can`t %s mrpa registry because %s id null!";
        if (organisationRepository.findOrganisationById(organisationId) == null) {
            throw new BadRequestException(String.format(errorMessage, operationType, "organisation", organisationId));
        }
        if (principalRepository.findPrincipalById(principalId) == null) {
            throw new BadRequestException(String.format(errorMessage, operationType, "principal", principalId));
        }
        if (representativeRepository.findRepresentativeById(representativeId) == null) {
            throw new BadRequestException(String.format(errorMessage, operationType, "representative", representativeId));
        }
    }

    @Override
    public MrpaRegistry recallMrpaRegistryById(Long id) {
        MrpaRegistryWithFioDTO mrpaRegistry = this.mrpaRegistryRepository.findMrpaRegistryById(id);
        if (mrpaRegistry == null) {
            throw new MchdException(String.format("Can`t recall mrpa registry because mrpa registry %s", NOT_FOUND));
        }
        return this.mrpaRegistryRepository.recallMrpaRegistryById(id);
    }
}

