package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioListDto;
import su.opencode.digsigcik.synapi.dto.m4d.MrpaRegistryFullXML;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO;
import su.opencode.digsigcik.synapi.model.*;

import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.util.List;

public interface MrpaRegistryService {
    List<MrpaRegistryWithFioListDto> findAllMrpaRegistry();

    MrpaRegistry saveMrpaRegistry(@NotNull MrpaRegistry mrpaRegistry);

    MrpaRegistry saveAllDataMrpaRegistry(MrpaRegistry mrpaRegistry, Organisation organisation, Principal principal, Representative representative, RepresentativePassport representativePassport, List<Credential> credentialList, List<Long> credentials);

    MrpaRegistry updateAllDataMrpaRegistry(MrpaRegistry mrpaRegistry, Organisation organisation, Principal principal, Representative representative, RepresentativePassport representativePassport, List<Credential> credentialList, List<Long> credentials);

    MrpaRegistryWithFioDTO findByMrpaRegistryId(@NotNull Long id);

    Integer findMrpaRegistryByGuid(@NotNull String guid);

    MrpaRegistryFullDTO findMrpaRegistryFullById(Long id);

    MrpaRegistry findById(@NotNull Long id);

    MrpaRegistry updateMrpaRegistry(@NotNull MrpaRegistry mrpaRegistry);

    void deleteMrpaRegistry(@NotNull Long id);

    MrpaRegistryWithFioDTO findByMrpaRegistryPrincipalInn(String inn);

    void recallMrpaRegistryById(@NotNull Long id);

    List<MrpaRegistryWithFioDTO.CredentialDTO> getCredentialByMrpaRegistryId(Long mrpaId);

    List<MrpaRegistryFullDTO.CredentialDTO> getCredentialByMrpaRegistryFullId(Long mrpaId);

    MrpaRegistryFullXML ConvertMrpaRegistryFullDtoToFullXml(MrpaRegistryFullDTO mrpaRegistryFullDTO) throws ParseException;

    void saveMrpaXml(Long id, String xmlData, String xmlFilename);
    List<MrpaRegistryWithFioListDto> findAllMrpaRegistryByLogin(String login);
}
