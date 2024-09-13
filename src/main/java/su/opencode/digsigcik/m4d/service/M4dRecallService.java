package su.opencode.digsigcik.m4d.service;

import su.opencode.digsigcik.m4d.dto.M4dRecallXml;
import su.opencode.digsigcik.m4d.dto.M4dResponseDto;
import su.opencode.digsigcik.m4d.model.M4d;
import su.opencode.digsigcik.m4d.model.M4dRecall;

import java.text.ParseException;
import java.util.List;

public interface M4dRecallService {
    List<M4dResponseDto> findAllMrpaRegistriesWithRecall() throws ParseException;
    M4d findMrpaRecallById(Long id);
    M4dRecall findById(Long id);
    void deleteMrpaRecallById(Long id);
    M4dRecall updateMrpaRecall(M4dRecall mrpaRecall);
    List<M4dResponseDto> findAllMrpaRecallByLogin(String login);
    M4dRecallXml ConvertMrpaRecallToXml(M4dResponseDto mrpaRegistryResponseDto);
}
