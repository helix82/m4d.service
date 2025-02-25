package su.opencode.digsigcik.m4d.service;

import org.springframework.security.core.Authentication;
import su.opencode.digsigcik.m4d.dto.M4dRegistryResponseDto;
import su.opencode.digsigcik.m4d.dto.M4dRevokeXml;
import su.opencode.digsigcik.m4d.model.M4dRecall;

import java.text.ParseException;
import java.util.List;

public interface M4dRecallService {
    M4dRecall saveM4dRecall(M4dRecall m4dRecall, String xmlString);
    List<M4dRegistryResponseDto> findAllMrpaRegistriesWithRecall() throws ParseException;
    M4dRecall findMrpaRecallById(Long id);
    M4dRecall findMrpaRecallByM4dId(Long id);
    M4dRecall findById(Long id);
    void deleteMrpaRecallById(Long id);
    void updateMrpaRecallById(Long id, Long fileId);
    M4dRecall updateMrpaRecall(M4dRecall mrpaRecall);
    List<M4dRegistryResponseDto> findAllM4dRevokeUsers(Authentication authentication);
    M4dRevokeXml convertMrpaRecallToXml(M4dRegistryResponseDto mrpaRegistryResponseDto);
}
