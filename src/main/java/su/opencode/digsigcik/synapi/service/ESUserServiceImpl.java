package su.opencode.digsigcik.synapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.SigHashDto;
import su.opencode.digsigcik.synapi.dto.UserDto;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.exception.SesException;
import su.opencode.digsigcik.synapi.model.ESSUser;
import su.opencode.digsigcik.synapi.repository.ESUserRepository;
import su.opencode.digsigcik.synapi.util.HashUtils;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static su.opencode.digsigcik.synapi.util.Constant.EMPTY_BYTE_ARRAY_ERROR;
import static su.opencode.digsigcik.synapi.util.Constant.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ESUserServiceImpl implements ESUserService {
    private final ESUserRepository esUserRepository;

    @Override
    public SigHashDto generateHashForPinCodeAndFile(String pinCode, MultipartFile file) {
        SigHashDto sigHashDto;
        try {
            String pinCodeHash = HashUtils.getSha256Hash(pinCode.getBytes(StandardCharsets.UTF_8));
            String fileHash = HashUtils.getSha256Hash(file.getBytes());

            sigHashDto = SigHashDto.builder()
                    .pinCodeHash(pinCodeHash)
                    .fileHash(fileHash)
                    .build();
        } catch (IOException e) {
            throw new SesException(EMPTY_BYTE_ARRAY_ERROR, e.getCause());
        }
        return sigHashDto;
    }

//    @Override
//    public boolean isESSUserWithPinCodeHashNotExist(String pinCodeHash) {
//        return getESSUserByUserPinCodeHash(pinCodeHash) == null;
//    }

    @Override
    public boolean isESSUserPasswordCorrect(String login, String password) {
        ESSUser user = esUserRepository.findESSUserByLogin(login);
        return user != null && password.equals(user.getPassword());
    }

    @Override
    public UserDto getESSUserByLogin(String login) {
        return new ObjectMapper().convertValue(this.esUserRepository.findESSUserByLogin(login), UserDto.class);
    }

//    @Override
//    public UserDto getESSUserByUserPinCodeHash(String pinCodeHash) {
//        return new ObjectMapper().convertValue(this.esUserRepository.findESSUserByPinCodeHash(pinCodeHash), UserDto.class);
//    }

    @Override
    public ESSUser createESSUser(@NotNull ESSUser essUser){
        return this.esUserRepository.save(essUser);
    }

    @Override
    public ESSUser updateESSUser(@NotNull ESSUser essUser) {
        if (this.esUserRepository.findESSUserByLogin(essUser.getLogin()) == null) {
            throw new BadRequestException("Can`t update ESS user because ESS user id null!");
        }
        Long essUserId = essUser.getId();

        if (essUserId == null) {
            throw new BadRequestException("Can`t update ESS user because id null!");
        }

        ESSUser existESSUser = this.esUserRepository.findESSUserById(essUserId);
        if (existESSUser == null) {
            throw new MchdException(String.format("Can`t update. ESS user with id=%d %s", essUserId, NOT_FOUND));
        }

        return this.esUserRepository.save(existESSUser);
    }

    @Override
    public void deleteESSUser(Long id){
        ESSUser essUser = this.esUserRepository.findESSUserById(id);
        if (essUser == null) {
            throw new MchdException(String.format("Can`t delete ESS user because ESS user %s", NOT_FOUND));
        }

        this.esUserRepository.deleteById(id);
    }
}
