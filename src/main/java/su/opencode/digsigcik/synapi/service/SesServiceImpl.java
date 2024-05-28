package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.ErrorDto;
import su.opencode.digsigcik.synapi.dto.JwtPayloadDto;
import su.opencode.digsigcik.synapi.dto.ResponseErrorDto;
import su.opencode.digsigcik.synapi.dto.SesResponseDto;
import su.opencode.digsigcik.synapi.dto.SesVerifyDto;
import su.opencode.digsigcik.synapi.dto.SesXmlDto;
import su.opencode.digsigcik.synapi.dto.SigHashDto;
import su.opencode.digsigcik.synapi.dto.UserDto;
import su.opencode.digsigcik.synapi.exception.SesException;
import su.opencode.digsigcik.synapi.util.HashUtils;
import su.opencode.digsigcik.synapi.util.JwtTokenUtils;
import su.opencode.digsigcik.synapi.util.UsersInfoUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import static su.opencode.digsigcik.synapi.util.Constant.EMPTY_BYTE_ARRAY_ERROR;
import static su.opencode.digsigcik.synapi.util.Constant.FILE_ERROR_MESSAGE;
import static su.opencode.digsigcik.synapi.util.Constant.OPERATION_ID_ERROR_MESSAGE;
import static su.opencode.digsigcik.synapi.util.Constant.PIN_CODE_ERROR_MESSAGE;
import static su.opencode.digsigcik.synapi.util.Constant.TOKEN_ERROR_MESSAGE;
import static su.opencode.digsigcik.synapi.util.Constant.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class SesServiceImpl implements SesService {
    private final UsersInfoUtils usersInfo;
    private final JwtTokenUtils jwtTokenUtils;

    @Override
    public SesResponseDto generateSesXmlFile(String pinCode, MultipartFile file, String token) {
        //todo спросить за варианты ответа, какое сообщение будет для ошибки
        if (isParameterNullOrIsEmpty(pinCode)) {
            return createSesResponseDto(PIN_CODE_ERROR_MESSAGE, HttpStatus.BAD_REQUEST);
        }
        if (isParameterNullOrIsEmpty(token)) {
            return createSesResponseDto(TOKEN_ERROR_MESSAGE, HttpStatus.BAD_REQUEST);
        }
        if (isParameterNullOrIsEmpty(file)) {
            return createSesResponseDto(FILE_ERROR_MESSAGE, HttpStatus.BAD_REQUEST);
        }

        //todo добавить методы в JwtTokenUtils для получения полей токена
        JwtPayloadDto jwtPayloadDto = jwtTokenUtils.parseJwtPayload(token);
        String userLogin = jwtPayloadDto.getPreferredUsername();

        Optional<UserDto> userDto = usersInfo.getUserByUserLogin(userLogin);
        if (userDto.isEmpty()) {
            return createSesResponseDto(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        UserDto user = userDto.get();


        /*todo работа с хэшами:
            1 - сравнить хэш пиндока который приходит с тем что записан для юзера
            2 - сгенерировать хэш документа (pin_hash.doc_hash)
            3 - сгенирировать хэш сигнатуры (pin_hash.doc_hash.timestamp)
        */
        SigHashDto sigHashDto = usersInfo.generateHashForPinCodeAndFile(pinCode, file);
        String pinCodeHash = sigHashDto.getPinCodeHash();
        if (usersInfo.isUserWithPinCodeHashNotExist(pinCodeHash)) {
            return createSesResponseDto("Invalid PIN code!", HttpStatus.NOT_FOUND);
        }

        LocalDate operationDate = LocalDate.now();
        usersInfo.setUserOperationDate(operationDate, userLogin);

        String signature = generateSignature(pinCodeHash, sigHashDto.getFileHash(), operationDate);
        usersInfo.setSignature(userLogin, signature);

        /*todo работа с xml(название файла - ses-дата.xml):
            1 - сформировать ses-дата.xml
            2 - добавить поля токена в ses-дата.xml файл
        */

        /*todo работа с БД:
            1 - добавить в таблицу sesSig новую запись:
                - сгенерировать UUID
                - добавить UUID, ses-дата.xml
            2 - добавить новую запись в таблицу ses:
                - указать id либо UUID для поля "sesXmlSig"
                - указать operationId, userlogin, signature
            3 - седлать controller, который возвращает ses-дата.xml по его уникальному UUID:
                принимает UUID, возвращает ses-дата.xml
        */

        //todo сформировать SesResponseDto для успешного ответа(json)

        //todo добавить работу с контейнером, класть в контейнер ses-дата.xml этот метод будет возращать контейнер

        return SesResponseDto.builder()
                .responseSesXml(new SesXmlDto())
                .build();
    }

    @Override
    public SesVerifyDto verifySes(String operationId, MultipartFile file) {
        throwExceptionWhenParameterNullOrIsEmpty(operationId, OPERATION_ID_ERROR_MESSAGE);
//        throwExceptionWhenFileNullOrIsEmpty(file);
        return getVerifyResultDto(operationId, file);
    }

    private String generateSignature(String pinCodeHash, String fileHash, LocalDate operationDate) {
        return String.format("%s.%s.%s", pinCodeHash, fileHash, operationDate);
    }

    private SesResponseDto createSesResponseDto(String errorMessage, HttpStatus responseStatus) {
        return SesResponseDto.builder()
                .error(ErrorDto.builder()
                        .error(new ResponseErrorDto(errorMessage, responseStatus.value()))
                        .build())
                .status(responseStatus)
                .build();
    }
    private void throwExceptionWhenParameterNullOrIsEmpty(String parameter, String errorMessage) {
        if (parameter == null || parameter.isEmpty()) {
            throw new SesException(errorMessage);
        }
    }

    private boolean isParameterNullOrIsEmpty(String parameter) {
        return parameter == null || parameter.isEmpty();
    }

    private boolean isParameterNullOrIsEmpty(MultipartFile file) {
        return file == null || file.isEmpty();
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
                    if (userDto.getSignature().equals(userPinCodeHash.concat(fileHash))) {
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
