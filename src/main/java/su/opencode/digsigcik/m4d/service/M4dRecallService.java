package su.opencode.digsigcik.m4d.service;

import org.springframework.security.core.Authentication;
import su.opencode.digsigcik.m4d.dto.M4dResponseDto;
import su.opencode.digsigcik.m4d.dto.M4dRevokeXml;
import su.opencode.digsigcik.m4d.model.M4d;
import su.opencode.digsigcik.m4d.model.M4dRecall;

import java.text.ParseException;
import java.util.List;

public interface M4dRecallService {
    List<M4dResponseDto> findAllMrpaRegistriesWithRecall() throws ParseException;
    M4d findMrpaRecallById(Long id);
    M4dRecall findById(Long id);
    void deleteMrpaRecallById(Long id);
    void updateMrpaRecallById(Long id, Long fileId);
    M4dRecall updateMrpaRecall(M4dRecall mrpaRecall);
    List<M4dResponseDto> findAllM4dRevokeUsers(Authentication authentication);
    M4dRevokeXml ConvertMrpaRecallToXml(M4dResponseDto mrpaRegistryResponseDto);
}
