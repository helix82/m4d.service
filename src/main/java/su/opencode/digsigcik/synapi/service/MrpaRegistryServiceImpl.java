package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO;
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
    public List<MrpaRegistry> findAllMrpaRegistry() {
        return this.mrpaRegistryRepository.findAll();
    public List<MrpaRegistryWithFioDTO>  findAllMrpaRegistry() {
        return this.repository.findAllMrpaRegistries();
    }

    @Override
    public MrpaRegistry saveMrpaRegistry(@NotNull MrpaRegistry mrpaRegistry) {
        throwExceptionIfRecordWithIdNotExist(mrpaRegistry, "create" );
        return this.mrpaRegistryRepository.save(mrpaRegistry);
    }

    @Override
    public List<MrpaRegistryWithFioDTO> findByMrpaRegistryId(Long id) {
        return this.repository.findMrpaRegistryById(id);
    public List<Map<String, Object>> findByMrpaRegistryId(@NotNull Long id) {
        List<Map<String, Object>> mrpaRegistry = this.mrpaRegistryRepository.findMrpaRegistryById(id);
        if (mrpaRegistry.isEmpty()) {
            throw new MchdException(String.format("Mrpa registry %s", NOT_FOUND));
        }
        return mrpaRegistry;
    }

    @Override
    public List<MrpaRegistryWithFioDTO> findAllByMrpaRegistryUserId(Long userId) {
        return this.repository.findAllMrpaRegistriesByUserId(userId);
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
        return this.mrpaRegistryRepository.save(mrpaRegistry);
    }

    @Override
    public void deleteMrpaRegistry(@NotNull Long id) {
        List<Map<String, Object>> mrpaRegistry = this.mrpaRegistryRepository.findMrpaRegistryById(id);
        if (mrpaRegistry.isEmpty()) {
            throw new MchdException(String.format("Can`t delete mrpa registry because mrpa registry %s", NOT_FOUND));
        }
        this.mrpaRegistryRepository.deleteById(id);
    }

    @Override
    public List<MrpaRegistryWithFioDTO> findByMrpaRegistryPrincipalInn(String inn) {
        return this.repository.findMrpaRegistriesByPrincipalInn(inn);
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
