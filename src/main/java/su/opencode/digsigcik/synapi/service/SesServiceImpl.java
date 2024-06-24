package su.opencode.digsigcik.synapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.api.model.ContainerPart;
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.api.model.ContainerSelectorCode;
import su.opencode.digsigcik.synapi.dto.*;
import su.opencode.digsigcik.synapi.enums.HashAlgorithm;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.exception.SesException;
import su.opencode.digsigcik.synapi.exception.SesVerifyException;
import su.opencode.digsigcik.synapi.model.EncryptionKeyEntity;
import su.opencode.digsigcik.synapi.model.Ses;
import su.opencode.digsigcik.synapi.model.SesContainer;
import su.opencode.digsigcik.synapi.repository.ESUserRepository;
import su.opencode.digsigcik.synapi.repository.EncryptionKeyRepository;
import su.opencode.digsigcik.synapi.repository.SesContainerRepository;
import su.opencode.digsigcik.synapi.repository.SesRepository;
import su.opencode.digsigcik.synapi.util.DateUtils;
import su.opencode.digsigcik.synapi.util.FileUtils;
import su.opencode.digsigcik.synapi.util.HashUtils;
import su.opencode.digsigcik.synapi.util.JwtTokenUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.*;

import static su.opencode.digsigcik.synapi.util.Constant.*;

@Service

public class SesServiceImpl implements SesService {
    private final SesContainerService sesContainerService;
    private final ContainerService containerService;
    private final ESUserService esUserService;
    private final JwtTokenUtils jwtTokenUtils;
    private final SesRepository sesRepository;
    private final ESUserRepository esUserRepository;
    private final SesContainerRepository sesContainerRepository;
    private final EncryptionKeyRepository encryptionKeyRepository;
    private final ElectronicSignatureService signatureService;

    public SesServiceImpl(SesContainerService sesContainerService, ContainerService containerService,
                          ESUserService esUserService, JwtTokenUtils jwtTokenUtils, SesRepository sesRepository,
                          ESUserRepository esUserRepository, SesContainerRepository sesContainerRepository,
                          EncryptionKeyRepository encryptionKeyRepository,
                          ElectronicSignatureService signatureService) {
        this.sesContainerService = sesContainerService;
        this.containerService = containerService;
        this.esUserService = esUserService;
        this.jwtTokenUtils = jwtTokenUtils;
        this.sesRepository = sesRepository;
        this.esUserRepository = esUserRepository;
        this.sesContainerRepository = sesContainerRepository;
        this.encryptionKeyRepository = encryptionKeyRepository;
        this.signatureService = signatureService;
    }

    @Override
    public SesResponseDto getContainerPath(MultipartFile srcFile, String token, HashAlgorithm hashAlgorithm) throws IOException {
//        if (isParameterNullOrEmpty(pinCode)) {
//            throw new SesException(PIN_CODE_ERROR_MESSAGE);
//        }
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
        //       SigHashDto sigHashDto = esUserService.generateHashForPinCodeAndFile(pinCode, srcFile);
//        String pinCodeHash = sigHashDto.getPinCodeHash();
//        if (esUserService.isESSUserWithPinCodeHashNotExist(pinCodeHash)) {
//            throw new SesException(PIN_CODE_ERROR_MESSAGE);
//        }

        LocalDateTime date = LocalDateTime.now();
        String operationDate = date.toString();

        Optional<EncryptionKeyEntity> keyEntity = encryptionKeyRepository.findById(1L);
        if (keyEntity.isEmpty()) {
            throw new RuntimeException("keyEntity not found");
        } else {
            //PinCode pinCode = pinCodeService.getPinCodeById(pinCodeId);
            PrivateKey privateKey = signatureService.getPrivateKey(keyEntity.get()
                    .getPrivateKey(), keyEntity.get()
                    .getPinCode()
                    .getPinCode(), keyEntity.get()
                    .getAlgorithm()
                    .getAlgorithmName());

            if (privateKey == null) {
                throw new RuntimeException("privateKey not found");
            }
            PublicKey publicKey = signatureService.getPublicKey(keyEntity.get()
                    .getPublicKey(), keyEntity.get()
                    .getAlgorithm()
                    .getAlgorithmName());
            if (publicKey == null) {
                throw new RuntimeException("publicKey not found");
            }
            byte[] signature = signatureService.createSignature(srcFile.getBytes(), privateKey);

//
//        String fileHash = sigHashDto.getFileHash();
//        String signature = generateSignature(sigHashDto.getPinCodeHash(), fileHash, operationDate);

//        ESSUser essUser = esUserRepository.findESSUserByLogin(userLogin);
//        essUser.setOperationDate(operationDate);
//        essUser.setFileHash(fileHash);
//        essUser.setSignature(signature);
//        esUserService.updateESSUser(essUser);

            // создаем файл temp.sig в который записываем signature
            String sourceDirectoryPathName = String.format("%s%s", ROOT_FILE_STORAGE_PATH, SOURCE_DIRECTORY_PATH);
            String sigFilePathName = String.format("%s%s", sourceDirectoryPathName, "temp.sig");
            FileUtils.createDirectoryIfSheNotExist(sourceDirectoryPathName);
            FileUtils.createFile(sigFilePathName, Base64.getEncoder()
                    .encodeToString(signature)
                    .getBytes());
            // создаем файл temp.pub в который записываем publicKey
            String pubFilePathName = String.format("%s%s", sourceDirectoryPathName, "temp.pub");
            FileUtils.createFile(pubFilePathName, Base64.getEncoder()
                    .encodeToString(publicKey.getEncoded())
                    .getBytes());

            MultipartFile sigMultipartFile = FileUtils.mapFileToMultipart(sigFilePathName);

            MultipartFile pubMultipartFile = FileUtils.mapFileToMultipart(pubFilePathName);

            String userFirstName = jwtPayloadDto.getFirstName();
            String userLastName = jwtPayloadDto.getLastName();
            String userPatronymic = jwtPayloadDto.getPatronymic();

            ContainerUserInfoDto containerUserInfo = ContainerUserInfoDto.builder()
                    .login(userLogin)
                    .firstname(userFirstName)
                    .lastname(userLastName)
                    .patronymic(userPatronymic)
                    .timestamp(date)
                    .signatureAlgorithm(keyEntity.get()
                            .getAlgorithm()
                            .getAlgorithmName())
                    .build();

            Resource containerResource = containerService.createPdfContainer(srcFile, sigMultipartFile,
                    pubMultipartFile,
                    containerUserInfo, hashAlgorithm.getAlgorithmName());

            File sigFile = new File(sigFilePathName);
            if (sigFile.exists()) {
                FileUtils.deleteFile(sigFilePathName);
            }

            File pubFile = new File(pubFilePathName);
            if (pubFile.exists()) {
                FileUtils.deleteFile(pubFilePathName);
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
                    // .fileHash(HashUtils.getSha256Hash(srcFile.getBytes()))
                    .signature(Base64.getEncoder()
                            .encodeToString(signature))
                    .operationDate(operationDate)
                    .sesContainerFileId(sesContainer.getId())
                    .build();

            boolean isSesCreated = createSes(sesDto);
            if (!isSesCreated) {
                throw new SesException("Ses not created!");
            }

            return SesResponseDto.builder()
                    .containerUUID(sesContainer.getUuid())
                    .sigType(keyEntity.get()
                            .getAlgorithm()
                            .getAlgorithmName())
                    .firstName(userFirstName)
                    .lastName(userLastName)
                    .patronymic(userPatronymic)
                    .build();
        }
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
        try {
            return getVerifyResultDto(containerData);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean createSes(SesDto sesDto) {
        boolean sesIsSaved = false;
        if (sesDto != null) {
            Ses ses = new Ses();
            ses.setUserLogin(sesDto.getUserLogin());
            //  ses.setFileHash(sesDto.getFileHash());
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
        val detachedPub = createContainerSelector(ContainerSelectorCode.DETACHEDPUB);
        val signatureAlgorithm = createContainerSelector(ContainerSelectorCode.SIGNATURE_ALGORITHM);


        return List.of(firstName, lastName, patronymic, userLogin, timestamp, sourceFile, detachedSign, detachedPub,signatureAlgorithm);
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

    private SesVerifyJsonDto getVerifyResultDto(Map<String, ContainerPart> containerData) throws NoSuchAlgorithmException, InvalidKeySpecException {
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
        String publicKey = new String(containerData.get("detachedPub")
                .getData(), StandardCharsets.UTF_8);
        String signatureAlgorithm = new String(containerData.get("signatureAlgorithm")
                .getData(), StandardCharsets.UTF_8);
        byte[] srcFile = containerData.get("sourceFile")
                .getData();

//        UserDto user = esUserService.getESSUserByLogin(userLogin);
//        if (user == null) {
//            throw new SesVerifyException(USER_NOT_FOUND);
//        }

        SesVerifyJsonDto sesVerifyJsonDto = new SesVerifyJsonDto();

//        if (user.getLogin()
//                .equals(userLogin)) {
        //?     String pinCodeHash = user.getPinCodeHash();
        String pinCodeHash = ""; //TODO: temporary solution

        String fileHash = HashUtils.getSha256Hash(srcFile);

        SesDto ses = findSesByUserLogin(userLogin);
//            String signature = generateSignature(pinCodeHash, fileHash, ses.getOperationDate());
        // декодируем открытый ключ из Base64 в байты
        byte[] decodedPublicKey = Base64.getDecoder()
                .decode(publicKey);

        // создаём спецификацию ключа из байтов декодированного ключа
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedPublicKey);

        String keyAlgorithm = signatureAlgorithm;
        if (keyAlgorithm.endsWith("RSA")) {
            keyAlgorithm = "RSA";
        } else if (keyAlgorithm.endsWith("ECDSA")) {
            keyAlgorithm = "EC";
        } else if (keyAlgorithm.endsWith("DSA")) {
            keyAlgorithm = "DSA";
        }
        // создаём фабрику ключей
        KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);

        // генерируем открытый ключ из спецификации ключа
        PublicKey publicKey1 = keyFactory.generatePublic(keySpec);
        boolean valid = signatureService.verifySignature(srcFile, Base64.getDecoder()
                .decode(userSignature), publicKey1);

        if (valid) {
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
        //    }

        return sesVerifyJsonDto;
    }
}
