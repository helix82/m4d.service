package su.opencode.digsigcik.m4d.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import su.opencode.digsigcik.m4d.dto.*;
import su.opencode.digsigcik.m4d.enums.HashAlgorithm;
import su.opencode.digsigcik.m4d.model.*;

import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.List;

public interface M4dService {
    List<M4dRegistryResponseDto> findAllMrpaRegistry() throws ParseException;
    M4d saveMrpaRegistry(@NotNull M4d m4d);
    M4d updateMrpaRegistry(@NotNull M4d m4d);
    Long saveAllDataMrpaRegistry(M4d m4d, Organisation organisation, Principal principal, PrincipalDetail principalDetail, Representative representative, RepresentativeDetail representativePassport, List<CredentialDto> credentialList, List<Long> credentials, String xmlString);
    Long updateAllDataMrpaRegistry(M4d m4d, Organisation organisation, Principal principal, PrincipalDetail principalDetail, Representative representative, RepresentativeDetail representativePassport, List<CredentialDto> credentialList, List<Long> credentials);
    Integer countM4dByGuid(@NotNull String guid);
    M4d findMrpaRegistryByGuid(@NotNull String guid);
    M4d findById(@NotNull Long id);
    void deleteMrpaRegistry(@NotNull Long id);
    List<M4dRegistryResponseDto.CredentialDto> getCredentialByMrpaRegistryId(Long mrpaId);
    M4dFullXML convertMrpaRegistryFullDtoToFullXml(M4dRegistryResponseDto m4dFullDTO) throws ParseException;
    List<M4dRegistryResponseDto> findAllM4dUsers(Authentication authentication) throws ParseException;
    List<M4d> findM4dByRepresentativeInn(String inn);
    M4dRegistryResponseDto findM4dByIdAndRepresentativeInn(String inn, Long id);
    String getStatusM4d(Long id) throws ParseException;
    M4dRegistryResponseDto convertM4dToM4dResponseDto(M4d m4d, Boolean withM4dSigFile, Boolean withRecallSigFile, Boolean withM4dXmlFile, Boolean withRecallXmlFile) throws ParseException;
    ReportCheckStatusM4dDto verify(@RequestPart("srcFile") MultipartFile sourceFile) throws NoSuchAlgorithmException, ParseException, IOException;
    ReportCheckStatusM4dDto createM4dRequestSearchForFindRR(MultipartFile sourceFile) throws NoSuchAlgorithmException;
    ReportCheckStatusM4dDto getReportM4dRequestSearchForFindPep(MultipartFile sourceFile) throws NoSuchAlgorithmException;
    ReportCheckStatusM4dDto checkM4dInPep(MultipartFile sourceFile) throws ParseException;
    M4dVerifyStatus getStatusVerifyInFns(@NotNull String guid);
    M4dRegistryResponseDto parseM4dXml(@NotNull M4dXml m4dXml) throws ParseException;
    ByteArrayResource createContainer(Authentication authentication, MultipartFile sourceFile, MultipartFile sourceSig, HashAlgorithm hashAlgorithm) throws IOException;
    Long uploadMrpaRegistry(MultipartFile sourceFile) throws IOException, ParseException, SAXException, JAXBException;
    Long uploadSig(Authentication authentication, Long mrpaId, MultipartFile container, boolean verify) throws IOException, ParseException, JAXBException;
    ByteArrayResource createArchiveM4dSig(Authentication authentication, Long fileId, String filename) throws IOException;
    M4dFullXML convertM4dRequestDtoToFullXml(M4dRegistryRequestDto m4DRegistryRequestDto) throws ParseException;
    String getGuidFromXml(MultipartFile sourceFile);
    String getHashXmlM4d(MultipartFile sourceFile) throws Exception;
    ReportValidSchemeDto validXmlM4dScheme(MultipartFile sourceFile, String reqScheme) throws IOException, SAXException;
}
