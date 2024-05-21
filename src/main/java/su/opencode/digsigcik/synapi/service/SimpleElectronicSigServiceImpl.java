package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.SigHashDto;
import su.opencode.digsigcik.synapi.dto.UserDto;
import su.opencode.digsigcik.synapi.exception.SesException;
import su.opencode.digsigcik.synapi.util.UsersInfoUtils;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SimpleElectronicSigServiceImpl implements SimpleElectronicSigService {
    private static final String IS_NULL_OR_EMPTY_ERROR_MESSAGE = "is null or empty!";
    private final UsersInfoUtils usersInfo = new UsersInfoUtils();

    @Override
    public Long getOperationId(String pinCode, MultipartFile file) {
        if (pinCode == null || pinCode.isEmpty()) {
            throw new SesException(String.format("PIN code %s", IS_NULL_OR_EMPTY_ERROR_MESSAGE));
        }
        if (file == null || file.isEmpty()) {
            throw new SesException(String.format("File %s", IS_NULL_OR_EMPTY_ERROR_MESSAGE));
        }

        SigHashDto sigHashDto = usersInfo.generateHashForPinCodeAndFile(pinCode, file);
        String pinCodeHash = sigHashDto.getPinCodeHash();
        Optional<UserDto> userDto = usersInfo.getUserByUserPinCodeHash(pinCodeHash);
        if (userDto.isEmpty()) {
            throw new SesException("Invalid PIN code!");
        }
        UserDto user = userDto.get();
        usersInfo.setUserFileHashWithPinCodeHash(pinCodeHash, sigHashDto.getFileHash());
        usersInfo.setUserOperationDate(new Date(), user.getLogin());
        return user.getOperationId();
    }

    @Override
    public String verifySes(String userLogin) {
        return null;
    }
}
