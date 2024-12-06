package su.opencode.digsigcik.m4d.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.m4d.dto.CredentialDto;
import su.opencode.digsigcik.m4d.dto.M4dFullXML;
import su.opencode.digsigcik.m4d.dto.M4dResponseDto;
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
    Integer countM4dByGuid(@NotNull String guid);
    M4d findMrpaRegistryByGuid(@NotNull String guid);
    M4d findById(@NotNull Long id);
    void deleteMrpaRegistry(@NotNull Long id);
    List<M4dResponseDto.CredentialDTO> getCredentialByMrpaRegistryId(Long mrpaId);
    M4dFullXML ConvertMrpaRegistryFullDtoToFullXml(M4dResponseDto m4dFullDTO) throws ParseException;
    List<M4dResponseDto> findAllM4dUsers(Authentication authentication) throws ParseException;
    List<M4d> findM4dByRepresentativeInn(String inn);
    M4dResponseDto findM4dByIdAndRepresentativeInn(String inn, Long id);
    String getStatusM4d(Long id) throws ParseException;
    M4dResponseDto convertM4dToM4dResponseDto(M4d m4d, Boolean withM4dSigFile, Boolean withRecallSigFile) throws ParseException;
    ResponseEntity<?> verify(Authentication authentication, @RequestPart("srcFile") MultipartFile sourceFile) throws Exception;
    M4dVerifyStatus getStatusVerifyInFns(@NotNull String guid);
    M4dResponseDto parseM4dXml(@NotNull M4dXml m4dXml) throws ParseException;
}
