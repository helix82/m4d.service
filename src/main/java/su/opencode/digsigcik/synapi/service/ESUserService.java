package su.opencode.digsigcik.synapi.service;

import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.SigHashDto;
import su.opencode.digsigcik.synapi.dto.UserDto;
import su.opencode.digsigcik.synapi.model.ESSUser;

import javax.validation.constraints.NotNull;

public interface ESUserService {
    boolean isESSUserPasswordCorrect(String login, String password);

    UserDto getESSUserByLogin(String login);

    ESSUser updateESSUser(@NotNull ESSUser essUser);

    ESSUser createESSUser(@NotNull ESSUser essUser);

    void deleteESSUser(Long id);
}
