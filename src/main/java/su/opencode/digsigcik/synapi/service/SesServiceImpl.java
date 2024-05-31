package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.ContainerUserInfoDto;
import su.opencode.digsigcik.synapi.dto.ErrorDto;
import su.opencode.digsigcik.synapi.dto.JwtPayloadDto;
import su.opencode.digsigcik.synapi.dto.ResponseErrorDto;
import su.opencode.digsigcik.synapi.dto.SesContainerFileDto;
import su.opencode.digsigcik.synapi.dto.SesDto;
import su.opencode.digsigcik.synapi.dto.SesResponseDto;
import su.opencode.digsigcik.synapi.dto.SesVerifyResponseDto;
import su.opencode.digsigcik.synapi.dto.SigHashDto;
import su.opencode.digsigcik.synapi.dto.UserDto;
import su.opencode.digsigcik.synapi.exception.SesException;
import su.opencode.digsigcik.synapi.model.Ses;
import su.opencode.digsigcik.synapi.model.SesContainer;
import su.opencode.digsigcik.synapi.repository.SesRepository;
import su.opencode.digsigcik.synapi.util.FileUtils;
import su.opencode.digsigcik.synapi.util.JwtTokenUtils;
import su.opencode.digsigcik.synapi.util.UsersInfoUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static su.opencode.digsigcik.synapi.util.Constant.FILE_ERROR_MESSAGE;
import static su.opencode.digsigcik.synapi.util.Constant.OPERATION_ID_ERROR_MESSAGE;
import static su.opencode.digsigcik.synapi.util.Constant.PIN_CODE_ERROR_MESSAGE;
import static su.opencode.digsigcik.synapi.util.Constant.TOKEN_ERROR_MESSAGE;
import static su.opencode.digsigcik.synapi.util.Constant.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class SesServiceImpl implements SesService {
    private final SesContainerService sesContainerService;
    private final ContainerService containerService;
    private final UsersInfoUtils usersInfo;
    private final JwtTokenUtils jwtTokenUtils;
    private final SesRepository sesRepository;

    @Override
    public SesResponseDto getContainerPath(String pinCode, MultipartFile srcFile, String token) {
        //todo спросить за варианты ответа, какое сообщение будет для ошибки
        if (isParameterNullOrIsEmpty(pinCode)) {
            return createSesResponseDto(PIN_CODE_ERROR_MESSAGE, HttpStatus.BAD_REQUEST);
        }
        if (isParameterNullOrIsEmpty(token)) {
            return createSesResponseDto(TOKEN_ERROR_MESSAGE, HttpStatus.BAD_REQUEST);
        }
        if (isParameterNullOrIsEmpty(srcFile)) {
            return createSesResponseDto(FILE_ERROR_MESSAGE, HttpStatus.BAD_REQUEST);
        }

        //получаем поля токена из payload
        JwtPayloadDto jwtPayloadDto = jwtTokenUtils.parseJwtPayload(token);
        String userLogin = jwtPayloadDto.getPreferredUsername();

        Optional<UserDto> userDto = usersInfo.getUserByUserLogin(userLogin);
        if (userDto.isEmpty()) {
            return createSesResponseDto(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        /*  1 - сравниваем хэш пиндока который приходит с тем что записан для юзера
            2 - генерируем хэш документа (pin_hash.doc_hash)
            3 - генирируем хэш сигнатуры (pin_hash.doc_hash.timestamp)
        */
        SigHashDto sigHashDto = usersInfo.generateHashForPinCodeAndFile(pinCode, srcFile);
        String pinCodeHash = sigHashDto.getPinCodeHash();
        if (usersInfo.isUserWithPinCodeHashNotExist(pinCodeHash)) {
            return createSesResponseDto("Invalid PIN code!", HttpStatus.NOT_FOUND);
        }
        UserDto user = userDto.get();
        Long operationId = user.getOperationId();

        LocalDateTime date = LocalDateTime.now();
        String operationDate = date.toString();
        String formattedOperationDate = formatDate(date);

        String fileHash = sigHashDto.getFileHash();
        String signature = generateSignature(sigHashDto.getPinCodeHash(), fileHash, operationDate);

        usersInfo.setUserOperationDate(operationDate, userLogin);
        usersInfo.setFileHash(userLogin, fileHash);
        usersInfo.setSignature(userLogin, signature);

        // создаем файл SHA256sig.sig в который записываем signature
        String sigFilePathName = "./files/source/SHA256sig.sig";
        FileUtils.createFile(sigFilePathName, signature.getBytes());

        MultipartFile sigMultipartFile = FileUtils.mapFileToMultipart(sigFilePathName);
        ContainerUserInfoDto containerUserInfo = ContainerUserInfoDto.builder()
                .login(userLogin)
                .firstname(jwtPayloadDto.getFirstName())
                .lastname(jwtPayloadDto.getLastName())
                .patronymic(jwtPayloadDto.getPatronymic())
                .timestamp(formattedOperationDate)
                .build();

        Resource containerResource = containerService.createPdfContainer(srcFile, sigMultipartFile, containerUserInfo);

        FileUtils.deleteFile(sigFilePathName);

        /*  - добавляем в таблицу sesContainer новую запись
            - добавляем в таблицу ses новую запись
        */
        String containerPath = String.format("./files/dscp/container-%s.dscp", formattedOperationDate);
        SesContainer sesContainer = addNewSesContainer(containerPath);
        SesDto sesDto = SesDto.builder()
                .operationId(operationId)
                .userLogin(userLogin)
                .fileHash(fileHash)
                .signature(signature)
                .sesContainerFileId(sesContainer.getId())
                .build();
        boolean isSesCreated = createSes(sesDto);
        if (!isSesCreated) {
            throw new SesException("Ses not created!");
        }

        return SesResponseDto.builder()
                .containerPath(containerPath)
                .build();
    }

    @Override
    public SesVerifyResponseDto verifySes(String operationId, MultipartFile container) {
        throwExceptionWhenParameterNullOrIsEmpty(operationId, OPERATION_ID_ERROR_MESSAGE);
        return getVerifyResultDto(operationId);
    }

    @Override
    public boolean createSes(SesDto sesDto) {
        boolean sesIsSaved = false;
        if (sesDto != null) {
            Ses ses = new Ses();
            ses.setOperationId(sesDto.getOperationId());
            ses.setUserLogin(sesDto.getUserLogin());
            ses.setFileHash(sesDto.getFileHash());
            ses.setSignature(sesDto.getSignature());
            ses.setContainerId(sesDto.getSesContainerFileId());
            sesRepository.save(ses);
            sesIsSaved = true;
        }
        return sesIsSaved;
    }

    @Override
    public Ses findSesById(Long id) {
        return sesRepository.findSesById(id);
    }

    @Override
    public boolean updateSes(SesDto ses) {
        return createSes(ses);
    }

    @Override
    public void deleteSes(Long id) {
        sesRepository.deleteById(id);
    }

    private SesContainer addNewSesContainer(String sesContainerPath) {
        UUID xmlUUID = UUID.randomUUID();
        SesContainerFileDto sesContainerFileDto = SesContainerFileDto.builder()
                .uuid(xmlUUID)
                .sesContainerPath(sesContainerPath)
                .build();
        return sesContainerService.createSesContainer(sesContainerFileDto);
    }

    private String generateSignature(String pinCodeHash, String fileHash, String operationDate) {
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

    private String formatDate(LocalDateTime operationDate) {
        return String.format("%s-%s-%sT%s.%s.%s",
                operationDate.getYear(), operationDate.getMonthValue(), operationDate.getDayOfMonth(),
                operationDate.getHour(), operationDate.getMinute(), operationDate.getSecond());
    }

    private SesVerifyResponseDto getVerifyResultDto(String operationId) {
        //todo нужно сначала брать пользователя по логину а потом у него уже сравнивать id операции с тем который передаем
        Optional<UserDto> user = usersInfo.getUserByOperationId(Long.valueOf(operationId));
        if (user.isEmpty()) {
            throw new SesException("Operation not found!");
        }
        return new SesVerifyResponseDto();
    }
}
