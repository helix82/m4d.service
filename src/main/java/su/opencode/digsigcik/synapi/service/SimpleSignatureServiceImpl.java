package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.api.model.ContainerPart;
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.api.model.ContainerSelectorCode;
import su.opencode.digsigcik.synapi.dto.*;
import su.opencode.digsigcik.synapi.exception.SesException;
import su.opencode.digsigcik.synapi.exception.SesVerifyException;
import su.opencode.digsigcik.synapi.model.ContainerFile;
import su.opencode.digsigcik.synapi.model.PinCode;
import su.opencode.digsigcik.synapi.model.UserProjection;
import su.opencode.digsigcik.synapi.repository.ContainerFileRepository;
import su.opencode.digsigcik.synapi.repository.PinCodeRepository;
import su.opencode.digsigcik.synapi.repository.UserProjectionRepository;
import su.opencode.digsigcik.synapi.util.DateUtils;
import su.opencode.digsigcik.synapi.util.FileUtils;
import su.opencode.digsigcik.synapi.util.HashUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static su.opencode.digsigcik.synapi.util.Constant.*;
import static su.opencode.digsigcik.synapi.util.HashUtils.generateHash;

@Service
@RequiredArgsConstructor
public class SimpleSignatureServiceImpl implements SimpleSignatureService {
    private final ContainerFileRepository containerFileRepository;
    private final ContainerService containerService;
    private final PinCodeRepository pinCodeRepository;
    private final UserProjectionRepository userProjectionRepository;

    @Override
    public SesResponseDto createSimpleSignature(String pinCode, MultipartFile srcFile, JwtPayloadDto jwtPayloadDto) {

        /*  1 - сравниваем хэш пиндока который приходит с тем что записан для юзера
            2 - генерируем хэш документа (pin_hash.doc_hash)
            3 - генирируем хэш сигнатуры (pin_hash.doc_hash.timestamp)
        */
        String userLogin = jwtPayloadDto.getPreferredUsername();
       // SigHashDto sigHashDto = esUserService.generateHashForPinCodeAndFile(pinCode, srcFile);
        UserProjection userProjection = userProjectionRepository.getUserProjectionByLogin(userLogin);
        if (userProjection == null) {
            throw new SesException(USER_PROJECTION_NOT_FOUND);
        }
        PinCode pinCode1 = pinCodeRepository.getPinCodeByUserProjection(userProjection);
        String pinCodeHash = generateHash(pinCode.getBytes(StandardCharsets.UTF_8),pinCode1.getAlgorithm());
        if (!pinCode1.getPinCodeHash()
                .equals(pinCodeHash)) {
            throw new SesException(PIN_CODE_ERROR_MESSAGE);
        }

        LocalDateTime date = LocalDateTime.now();
        String operationDateHash = generateHash(date.toString().getBytes(),pinCode1.getAlgorithm());
        String fileHash;
        try {
            fileHash = generateHash(srcFile.getBytes(), pinCode1.getAlgorithm());
        } catch (IOException e) {
            throw new SesException(EMPTY_BYTE_ARRAY_ERROR, e.getCause());
        }
        String signature = generateSignature(pinCodeHash, fileHash, operationDateHash);


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
                .signatureAlgorithm("CUSTOM")
                .build();

        containerService.createPdfContainer(srcFile, sigMultipartFile, null, containerUserInfo, pinCode1.getAlgorithm().getAlgorithmName());

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
        UUID xmlUUID = UUID.randomUUID();
        containerFileRepository.save(ContainerFile.builder()
                .uuid(xmlUUID.toString())
                .filePath(containerPath)
                .build());
        return SesResponseDto.builder()
                .containerUUID(xmlUUID.toString())
                .sigType("simple")
                .firstName(userFirstName)
                .lastName(userLastName)
                .patronymic(userPatronymic)
                .build();
    }


    @Override
    public SesVerifyJsonDto verifySimpleSignature(@NotNull MultipartFile container) {
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


    private String generateSignature(String pinCodeHash, String fileHash, String operationDate) {
        return String.format("%s.%s.%s", pinCodeHash, fileHash, operationDate);
    }


    private SesVerifyJsonDto getVerifyResultDto(Map<String, ContainerPart> containerData) {
        String userFirstname = new String(containerData.get("userFirstname")
                .getData(), StandardCharsets.UTF_8);
        String userLastname = new String(containerData.get("userLastname")
                .getData(), StandardCharsets.UTF_8);
        String userPatronymic = new String(containerData.get("userPatronymic")
                .getData(), StandardCharsets.UTF_8);
        String timestamp = new String(containerData.get("timestamp")
                .getData(), StandardCharsets.UTF_8);
        String userLogin = new String(containerData.get("userLogin")
                .getData(), StandardCharsets.UTF_8);
        String userSignature = new String(containerData.get("detachedSign")
                .getData(), StandardCharsets.UTF_8);
        byte[] srcFile = containerData.get("sourceFile")
                .getData();

        UserProjection userProjection = userProjectionRepository.getUserProjectionByLogin(userLogin);
        PinCode pinCode = pinCodeRepository.getPinCodeByUserProjection(userProjection);
        if (userProjection == null) {
            throw new SesVerifyException(JWT_USER_NOT_FOUND);
        }

        SesVerifyJsonDto sesVerifyJsonDto = new SesVerifyJsonDto();

        if (userProjection.getLogin()
                .equals(userLogin)) {
            String fileHash = HashUtils.generateHash(srcFile,pinCode.getAlgorithm());
            String operationDateHash = generateHash(timestamp.getBytes(),pinCode.getAlgorithm());
            String signature = generateSignature(pinCode.getPinCodeHash(), fileHash, operationDateHash);

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
