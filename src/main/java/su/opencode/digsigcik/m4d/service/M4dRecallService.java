package su.opencode.digsigcik.m4d.service;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.m4d.dto.M4dRegistryResponseDto;
import su.opencode.digsigcik.m4d.dto.M4dRevokeXml;
import su.opencode.digsigcik.m4d.model.M4dRecall;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public interface M4dRecallService {
    M4dRecall saveM4dRecall(M4dRecall m4dRecall, String xmlString) throws Exception;
    List<M4dRegistryResponseDto> findAllMrpaRegistriesWithRecall(Authentication authentication) throws ParseException;
    M4dRecall findMrpaRecallById(Long id);
    M4dRecall findMrpaRecallByM4dId(Long id);
    M4dRecall findById(Long id);
    void deleteMrpaRecallById(Long id);
    void updateMrpaRecallById(Long id, Long fileId);
    void updateStatusMrpaRecallById(Long id, String status);
    M4dRecall updateMrpaRecall(M4dRecall mrpaRecall);
    List<M4dRegistryResponseDto> findAllM4dRevokeUsers(Authentication authentication);
    M4dRevokeXml convertMrpaRecallToXml(M4dRegistryResponseDto mrpaRegistryResponseDto);
    Long uploadSig(Authentication authentication, Long id, MultipartFile container, boolean verify) throws IOException, ParseException;
    String getHashFromXmlString(String xmlString) throws Exception;
}
