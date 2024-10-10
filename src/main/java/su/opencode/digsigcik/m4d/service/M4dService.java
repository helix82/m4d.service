package su.opencode.digsigcik.m4d.service;

import org.springframework.http.ResponseEntity;
import su.opencode.digsigcik.m4d.dto.*;
import su.opencode.digsigcik.m4d.dto.response.*;
import su.opencode.digsigcik.m4d.model.*;

import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.util.List;

public interface M4dService {
    List<M4dResponseDto> findAllMrpaRegistry() throws ParseException;
    M4d saveMrpaRegistry(@NotNull M4d m4d);
    M4d updateMrpaRegistry(@NotNull M4d m4d);
    ResponseEntity<?> saveAllDataMrpaRegistry(M4d m4d, Organisation organisation, Principal principal, Representative representative, RepresentativePassport representativePassport, List<CredentialDto> credentialList, List<Long> credentials);
    ResponseEntity<?> updateAllDataMrpaRegistry(M4d m4d, Organisation organisation, Principal principal, Representative representative, RepresentativePassport representativePassport, List<CredentialDto> credentialList, List<Long> credentials);
    Integer findMrpaRegistryByGuid(@NotNull String guid);
    M4d findById(@NotNull Long id);
    void deleteMrpaRegistry(@NotNull Long id);
    List<M4dResponseDto.CredentialDTO> getCredentialByMrpaRegistryId(Long mrpaId);
    M4dFullXML ConvertMrpaRegistryFullDtoToFullXml(M4dResponseDto m4dFullDTO) throws ParseException;
    List<M4dResponseDto> findAllMrpaRegistryByLogin(String login) throws ParseException;
    String getStatusM4d(Long id) throws ParseException;
    M4dResponseDto convertM4dToM4dResponseDto(M4d m4d, Boolean withM4dSigFile, Boolean withRecallSigFile) throws ParseException;
}
