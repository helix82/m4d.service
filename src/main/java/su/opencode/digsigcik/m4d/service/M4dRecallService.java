package su.opencode.digsigcik.m4d.service;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.m4d.dto.M4dRegistryResponseDto;
import su.opencode.digsigcik.m4d.dto.M4dRevokeXml;
import su.opencode.digsigcik.m4d.dto.response.M4dRevokeResponseInfo;
import su.opencode.digsigcik.m4d.model.M4dRecall;

import java.text.ParseException;
import java.util.List;
import java.util.Set;

public interface M4dRecallService {
    M4dRecall saveM4dRecall(Authentication authentication, M4dRecall m4dRecall, String xmlString);
    List<M4dRegistryResponseDto> findAllMrpaRegistriesWithRecall(Authentication authentication) throws ParseException;
    M4dRecall findMrpaRecallByM4dId(Long id);
    M4dRecall findById(Long id);
    void deleteMrpaRecallById(Long id);
    void updateStatusMrpaRecallById(Long id, String status);
    M4dRecall updateMrpaRecall(M4dRecall mrpaRecall);
    List<M4dRegistryResponseDto> findAllM4dRevokeUsers(Authentication authentication);
    M4dRevokeXml convertMrpaRecallToXml(M4dRegistryResponseDto mrpaRegistryResponseDto);
    M4dRecall uploadSig(Authentication authentication, Long id, MultipartFile container) throws Exception;
    String getHashFromXmlString(String xmlString) throws Exception;
    String uploadResultSendM4dRevokeToRR(M4dRevokeResponseInfo m4dRevokeResponseInfo);
    long countM4dRevokeByInnsAndStatus(Set<String> inns, String status);
}
