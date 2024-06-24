package su.opencode.digsigcik.synapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.api.model.ContainerPart;
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.api.model.ContainerSelectorCode;
import su.opencode.digsigcik.synapi.dto.ContainerUserInfoDto;
import su.opencode.digsigcik.synapi.dto.JwtPayloadDto;
import su.opencode.digsigcik.synapi.dto.SesContainerFileDto;
import su.opencode.digsigcik.synapi.dto.SesDto;
import su.opencode.digsigcik.synapi.dto.SesResponseDto;
import su.opencode.digsigcik.synapi.dto.SesUserInfoDto;
import su.opencode.digsigcik.synapi.dto.SesVerifyJsonDto;
import su.opencode.digsigcik.synapi.dto.SigHashDto;
import su.opencode.digsigcik.synapi.dto.UserDto;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.exception.SesException;
import su.opencode.digsigcik.synapi.exception.SesVerifyException;
import su.opencode.digsigcik.synapi.model.ESSUser;
import su.opencode.digsigcik.synapi.model.PinCode;
import su.opencode.digsigcik.synapi.model.Ses;
import su.opencode.digsigcik.synapi.model.SesContainer;
import su.opencode.digsigcik.synapi.repository.ESUserRepository;
import su.opencode.digsigcik.synapi.repository.PinCodeRepository;
import su.opencode.digsigcik.synapi.repository.SesRepository;
import su.opencode.digsigcik.synapi.util.DateUtils;
import su.opencode.digsigcik.synapi.util.FileUtils;
import su.opencode.digsigcik.synapi.util.HashUtils;
import su.opencode.digsigcik.synapi.util.JwtTokenUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static su.opencode.digsigcik.synapi.util.Constant.DSCP_DIRECTORY_PATH;
import static su.opencode.digsigcik.synapi.util.Constant.FILE_ERROR_MESSAGE;
import static su.opencode.digsigcik.synapi.util.Constant.HASH_ALGORITHM;
import static su.opencode.digsigcik.synapi.util.Constant.PIN_CODE_ERROR_MESSAGE;
import static su.opencode.digsigcik.synapi.util.Constant.ROOT_FILE_STORAGE_PATH;
import static su.opencode.digsigcik.synapi.util.Constant.SOURCE_DIRECTORY_PATH;
import static su.opencode.digsigcik.synapi.util.Constant.TOKEN_ERROR_MESSAGE;
import static su.opencode.digsigcik.synapi.util.Constant.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class SesServiceImpl implements SesService {
    private final SesContainerService sesContainerService;
    private final ContainerService containerService;
    private final ESUserService esUserService;
    private final JwtTokenUtils jwtTokenUtils;
    private final SesRepository sesRepository;
    private final ESUserRepository esUserRepository;
    private final PinCodeRepository pinCodeRepository;

    @Override
    public SesResponseDto getContainerPath(String pinCode, MultipartFile srcFile, String token) {
        if (isParameterNullOrEmpty(pinCode)) {
            throw new SesException(PIN_CODE_ERROR_MESSAGE);
        }
        if (isParameterNullOrEmpty(token)) {
            throw new BadRequestException(TOKEN_ERROR_MESSAGE);
        }
        if (isParameterNullOrEmpty(srcFile)) {
            throw new BadRequestException(FILE_ERROR_MESSAGE);
        }

        //получаем поля токена из payload
        JwtPayloadDto jwtPayloadDto = jwtTokenUtils.parseJwtPayload(token);
        String userLogin = jwtPayloadDto.getPreferredUsername();

        UserDto userDto = esUserService.getESSUserByLogin(userLogin);
        if (userDto == null) {
            throw new SesException(USER_NOT_FOUND);
        }

        /*  1 - сравниваем хэш пиндока который приходит с тем что записан для юзера
            2 - генерируем хэш документа (pin_hash.doc_hash)
            3 - генирируем хэш сигнатуры (pin_hash.doc_hash.timestamp)
        */
        SigHashDto sigHashDto = esUserService.generateHashForPinCodeAndFile(pinCode, srcFile);
        String pinCodeHash = sigHashDto.getPinCodeHash();
        ESSUser essUser = esUserRepository.findESSUserByLogin(userLogin);
        PinCode pinCode1 = pinCodeRepository.getPinCodeByUser(essUser);
        if (!pinCode1.getPinCodeHash().equals(pinCodeHash)) {
            throw new SesException(PIN_CODE_ERROR_MESSAGE);
        }

        LocalDateTime date = LocalDateTime.now();
        String operationDate = date.toString();

        String fileHash = sigHashDto.getFileHash();
        String signature = generateSignature(sigHashDto.getPinCodeHash(), fileHash, operationDate);


        // создаем файл SHA256sig.sig в который записываем signature
        String sourceDirectoryPathName = String.format("%s%s", ROOT_FILE_STORAGE_PATH, SOURCE_DIRECTORY_PATH);
        String sigFilePathName = String.format("%s%s", sourceDirectoryPathName, "SHA256sig.sig");
        FileUtils.createDirectoryIfSheNotExist(sourceDirectoryPathName);
        FileUtils.createFile(sigFilePathName, signature.getBytes());

        MultipartFile sigMultipartFile = FileUtils.mapFileToMultipart(sigFilePathName);
        String userFirstName = jwtPayloadDto.getFirstName();
        String userLastName = jwtPayloadDto.getLastName();
        String userPatronymic = jwtPayloadDto.getPatronymic();

        ContainerUserInfoDto containerUserInfo = ContainerUserInfoDto.builder()
                .login(userLogin)
                .firstname(userFirstName)
                .lastname(userLastName)
                .patronymic(userPatronymic)
                .timestamp(date)
                .build();

        containerService.createPdfContainer(srcFile, sigMultipartFile, containerUserInfo);

        File sigFile = new File(sigFilePathName);
        if (sigFile.exists()) {
            FileUtils.deleteFile(sigFilePathName);
        }

        /*  - добавляем в таблицу sesContainer новую запись
            - добавляем в таблицу ses новую запись
        */
        String containerPath = String.format("%s%scontainer-%s.dscp", ROOT_FILE_STORAGE_PATH, DSCP_DIRECTORY_PATH,
                DateUtils.formatDate(date)
        );
        SesContainer sesContainer = addNewSesContainer(containerPath);
        SesDto sesDto = SesDto.builder()
                .userLogin(userLogin)
                .fileHash(fileHash)
                .signature(signature)
                .operationDate(operationDate)
                .sesContainerFileId(sesContainer.getId())
                .build();

        boolean isSesCreated = createSes(sesDto);
        if (!isSesCreated) {
            throw new SesException("Ses not created!");
        }

        return SesResponseDto.builder()
                .containerUUID(sesContainer.getUuid())
                .sigType("simple")
                .firstName(userFirstName)
                .lastName(userLastName)
                .patronymic(userPatronymic)
                .build();
    }

    @Override
    public SesVerifyJsonDto verifySes(@NotNull MultipartFile container) {
        if (container.isEmpty()) {
            throw new SesVerifyException("Container is empty!");
        }

        Map<String, ContainerPart> containerData = containerService.getContainerData(
                container, generateContainerSelectors(), false);

        if (containerData == null) {
            throw new SesVerifyException("Verify ses error!");
        }
        return getVerifyResultDto(containerData);
    }

    @Override
    public boolean createSes(SesDto sesDto) {
        boolean sesIsSaved = false;
        if (sesDto != null) {
            Ses ses = new Ses();
            ses.setUserLogin(sesDto.getUserLogin());
            ses.setFileHash(sesDto.getFileHash());
            ses.setSignature(sesDto.getSignature());
            ses.setOperationDate(sesDto.getOperationDate());
            ses.setContainerId(sesDto.getSesContainerFileId());

            sesRepository.save(ses);
            sesIsSaved = true;
        }
        return sesIsSaved;
    }

    @Override
    public SesDto findSesById(Long id) {
        return new ObjectMapper().convertValue(this.sesRepository.findSesById(id), SesDto.class);
    }

    @Override
    public SesDto findSesByUserLogin(String login) {
        return new ObjectMapper().convertValue(this.sesRepository.findSesByUserLogin(login), SesDto.class);
    }

    @Override
    public Ses updateSes(Ses ses) {
        return this.sesRepository.save(ses);
    }

    @Override
    public void deleteSes(Long id) {
        this.sesRepository.deleteById(id);
    }

    private List<ContainerSelector> generateContainerSelectors() {
        val firstName = createContainerSelector(ContainerSelectorCode.USERFIRSTNAME);
        val lastName = createContainerSelector(ContainerSelectorCode.USERLASTNAME);
        val patronymic = createContainerSelector(ContainerSelectorCode.USERPATRONYMIC);
        val userLogin = createContainerSelector(ContainerSelectorCode.USERLOGIN);
        val timestamp = createContainerSelector(ContainerSelectorCode.TIMESTAMP);
        val sourceFile = createContainerSelector(ContainerSelectorCode.SOURCEFILE);
        val detachedSign = createContainerSelector(ContainerSelectorCode.DETACHEDSIGN);

        return List.of(firstName, lastName, patronymic, userLogin, timestamp, sourceFile, detachedSign);
    }

    private ContainerSelector createContainerSelector(ContainerSelectorCode containerSelectorCode) {
        val selector = new ContainerSelector();
        selector.setCode(containerSelectorCode);
        return selector;
    }

    private SesContainer addNewSesContainer(String sesContainerPath) {
        UUID xmlUUID = UUID.randomUUID();
        SesContainerFileDto sesContainerFileDto = SesContainerFileDto.builder()
                .uuid(xmlUUID.toString())
                .sesContainerPath(sesContainerPath)
                .build();
        return sesContainerService.createSesContainer(sesContainerFileDto);
    }

    private String generateSignature(String pinCodeHash, String fileHash, String operationDate) {
        return String.format("%s.%s.%s", pinCodeHash, fileHash, operationDate);
    }

    private boolean isParameterNullOrEmpty(String parameter) {
        return parameter == null || parameter.isEmpty();
    }

    private boolean isParameterNullOrEmpty(MultipartFile file) {
        return file == null || file.isEmpty();
    }

    private SesVerifyJsonDto getVerifyResultDto(Map<String, ContainerPart> containerData) {
        String userFirstname = new String(containerData.get("userFirstname").getData(), StandardCharsets.UTF_8);
        String userLastname = new String(containerData.get("userLastname").getData(), StandardCharsets.UTF_8);
        String userPatronymic = new String(containerData.get("userPatronymic").getData(), StandardCharsets.UTF_8);
        String timestamp = new String(containerData.get("timestamp").getData(), StandardCharsets.UTF_8);
        String userLogin = new String(containerData.get("userLogin").getData(), StandardCharsets.UTF_8);
        String userSignature = new String(containerData.get("detachedSign").getData(), StandardCharsets.UTF_8);
        byte[] srcFile = containerData.get("sourceFile").getData();

        ESSUser essUser = esUserRepository.findESSUserByLogin(userLogin);
        PinCode pinCode = pinCodeRepository.getPinCodeByUser(essUser);
        if (essUser == null) {
            throw new SesVerifyException(USER_NOT_FOUND);
        }

        SesVerifyJsonDto sesVerifyJsonDto = new SesVerifyJsonDto();

        if (essUser.getLogin().equals(userLogin)) {
            String fileHash = HashUtils.getSha256Hash(srcFile);

            String signature = generateSignature(pinCode.getPinCodeHash(), fileHash, timestamp);

            if (userSignature.equals(signature)) {
                sesVerifyJsonDto.setVerifyResultMessage("VALID");
                sesVerifyJsonDto.setUserInfo(
                        SesUserInfoDto.builder()
                                .hashAlgorithmType(HASH_ALGORITHM)
                                .hashSignatureAlgorithm(HASH_ALGORITHM)
                                .firstname(userFirstname)
                                .lastname(userLastname)
                                .patronymic(userPatronymic)
                                .timestamp(LocalDateTime.parse(timestamp))
                                .build()
                );
            } else {
                throw new SesVerifyException("INVALID");
            }
        }

        return sesVerifyJsonDto;
    }
}
