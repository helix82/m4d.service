package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.SesVerifyDto;
import su.opencode.digsigcik.synapi.dto.SigHashDto;
import su.opencode.digsigcik.synapi.dto.UserDto;
import su.opencode.digsigcik.synapi.exception.SesException;
import su.opencode.digsigcik.synapi.exception.UserAuthException;
import su.opencode.digsigcik.synapi.util.HashUtils;
import su.opencode.digsigcik.synapi.util.JwtTokenUtils;
import su.opencode.digsigcik.synapi.util.UsersInfoUtils;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static su.opencode.digsigcik.synapi.util.Constant.EMPTY_BYTE_ARRAY_ERROR;
import static su.opencode.digsigcik.synapi.util.Constant.FILE_ERROR_MESSAGE;
import static su.opencode.digsigcik.synapi.util.Constant.LOGIN_ERROR_MESSAGE;
import static su.opencode.digsigcik.synapi.util.Constant.OPERATION_ID_ERROR_MESSAGE;
import static su.opencode.digsigcik.synapi.util.Constant.PIN_CODE_ERROR_MESSAGE;
import static su.opencode.digsigcik.synapi.util.Constant.TOKEN_ERROR_MESSAGE;

@Service
@RequiredArgsConstructor
public class SimpleElectronicSigServiceImpl implements SimpleElectronicSigService {
    private final UsersInfoUtils usersInfo;
    private final JwtTokenUtils jwtTokenUtils;

    @Override
    public Long getOperationId(String pinCode, MultipartFile file, String token) {
        throwExceptionWhenParameterNullOrIsEmpty(pinCode, PIN_CODE_ERROR_MESSAGE);
        throwExceptionWhenTokenNullOrIsEmpty(token);
        throwExceptionWhenFileNullOrIsEmpty(file);

        String authPayloadToken = jwtTokenUtils.getPayloadJwtToken(token.substring(7));
        String userLogin = getUserLoginFromPayloadToken(authPayloadToken);

        Optional<UserDto> userDto = usersInfo.getUserByUserLogin(userLogin);
        if (userDto.isEmpty()) {
            throw new UserAuthException(String.format("User with login %s does not exist!", userLogin));
        }

        SigHashDto sigHashDto = usersInfo.generateHashForPinCodeAndFile(pinCode, file);
        String pinCodeHash = sigHashDto.getPinCodeHash();
        if (usersInfo.isUserWithPinCodeHashNotExist(pinCodeHash)) {
            throw new SesException("Invalid PIN code!");
        }

        UserDto user = userDto.get();
        usersInfo.setUserFileHashWithPinCodeHash(pinCodeHash, sigHashDto.getFileHash());
        usersInfo.setUserOperationDate(new Date(), userLogin);
        return user.getOperationId();
    }

    @Override
    public SesVerifyDto verifySes(String operationId, MultipartFile file) {
        throwExceptionWhenParameterNullOrIsEmpty(operationId, OPERATION_ID_ERROR_MESSAGE);
        throwExceptionWhenFileNullOrIsEmpty(file);
        return getVerifyResultDto(operationId, file);
    }

    public void throwExceptionWhenTokenNullOrIsEmpty(String token) {
        if (token == null || token.isEmpty()) {
            throw new UserAuthException(TOKEN_ERROR_MESSAGE);
        }
    }

    private void throwExceptionWhenParameterNullOrIsEmpty(String parameter, String errorMessage) {
        if (parameter == null || parameter.isEmpty()) {
            throw new SesException(errorMessage);
        }
    }

    private void throwExceptionWhenFileNullOrIsEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new SesException(FILE_ERROR_MESSAGE);
        }
    }

    private String getUserLoginFromPayloadToken(String tokenPayload) {
        Pattern pattern = Pattern.compile("username\":\"(.*?)\",");
        Matcher matcher = pattern.matcher(tokenPayload);

        boolean isMatcherSuccessfulFindLogin = matcher.find();
        if (!isMatcherSuccessfulFindLogin) {
            throw new UserAuthException(LOGIN_ERROR_MESSAGE);
        }
        return matcher.group(1);
    }

    private SesVerifyDto getVerifyResultDto(String operationId, MultipartFile file) {
        //todo нужно сначала брать пользователя по логину а потом у него уже сравнивать id операции с тем который передаем
        Optional<UserDto> user = usersInfo.getUserByOperationId(Long.valueOf(operationId));
        if (user.isEmpty()) {
            throw new SesException("Operation not found!");
        }

        SesVerifyDto sesVerifyDto = new SesVerifyDto();
        String userPinCodeHash = user.get().getPinCodeHash();
        for (UserDto userDto : usersInfo.getUsersInfo()) {
            if (userDto.getPinCodeHash().equals(userPinCodeHash)){
                try {
                    String fileHash = HashUtils.getSha1Hash(file.getBytes());
                    if (userDto.getFileHash().equals(userPinCodeHash.concat(fileHash))) {
                        sesVerifyDto.setVerifyResultMessage("Подпись верна.");
                        sesVerifyDto.setUserLogin(user.get().getLogin());
                        sesVerifyDto.setOperationDate(user.get().getOperationDate());
                        break;
                    } else {
                        throw new SesException("Подпись не верна.");
                    }
                } catch (IOException e) {
                    throw new SesException(EMPTY_BYTE_ARRAY_ERROR, e.getCause());
                }
            }
        }
        return sesVerifyDto;
    }
}
