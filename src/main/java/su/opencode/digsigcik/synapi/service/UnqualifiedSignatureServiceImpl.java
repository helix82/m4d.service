package su.opencode.digsigcik.synapi.service;

import lombok.AllArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.api.model.ContainerPart;
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.api.model.ContainerSelectorCode;
import su.opencode.digsigcik.synapi.dto.*;
import su.opencode.digsigcik.synapi.enums.HashAlgorithm;
import su.opencode.digsigcik.synapi.exception.SesException;
import su.opencode.digsigcik.synapi.exception.SesVerifyException;
import su.opencode.digsigcik.synapi.model.EncryptionKeyEntity;
import su.opencode.digsigcik.synapi.model.ContainerFile;
import su.opencode.digsigcik.synapi.model.UserProjection;
import su.opencode.digsigcik.synapi.repository.ContainerFileRepository;
import su.opencode.digsigcik.synapi.repository.EncryptionKeyRepository;
import su.opencode.digsigcik.synapi.repository.UserProjectionRepository;
import su.opencode.digsigcik.synapi.util.DateUtils;
import su.opencode.digsigcik.synapi.util.FileUtils;

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
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static su.opencode.digsigcik.synapi.util.Constant.*;

@Service
@AllArgsConstructor
public class UnqualifiedSignatureServiceImpl implements UnqualifiedSignatureService {
    private final ContainerFileRepository containerFileRepository;
    private final ContainerService containerService;
    private final EncryptionKeyRepository encryptionKeyRepository;
    private final ElectronicSignatureService signatureService;
    private final UserProjectionRepository userProjectionRepository;

    @Override
    public SesResponseDto createUnqualifiedSignature(MultipartFile srcFile, JwtPayloadDto jwtPayloadDto,
                                                     HashAlgorithm hashAlgorithm) throws IOException {
        LocalDateTime date = LocalDateTime.now();
        String operationDate = date.toString();
        String userLogin = jwtPayloadDto.getPreferredUsername();
        UserProjection userProjection = userProjectionRepository.getUserProjectionByLogin(userLogin);
        if (userProjection == null) {
            throw new SesException(USER_PROJECTION_NOT_FOUND);
        }
        EncryptionKeyEntity keyEntity =
                encryptionKeyRepository.getEncryptionKeyEntitiesByUserProjection(userProjection);
        if (keyEntity == null) {
            throw new RuntimeException("keyEntity not found");
        } else {
            PrivateKey privateKey = signatureService.getPrivateKey(keyEntity
                    .getPrivateKey(), keyEntity
                    .getAlgorithm()
                    .getAlgorithmName());

            if (privateKey == null) {
                throw new RuntimeException("privateKey not found");
            }
            PublicKey publicKey = signatureService.getPublicKey(keyEntity
                    .getPublicKey(), keyEntity
                    .getAlgorithm()
                    .getAlgorithmName());
            if (publicKey == null) {
                throw new RuntimeException("publicKey not found");
            }
            byte[] signature = signatureService.createSignature(srcFile.getBytes(), privateKey);

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
                    .signatureAlgorithm(keyEntity
                            .getAlgorithm()
                            .getAlgorithmName())
                    .build();

            containerService.createPdfContainer(srcFile, sigMultipartFile,
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

            UUID xmlUUID = UUID.randomUUID();
            containerFileRepository.save(ContainerFile.builder()
                    .uuid(xmlUUID.toString())
                    .filePath(containerPath)
                    .build());
            return SesResponseDto.builder()
                    .containerUUID(xmlUUID.toString())
                    .sigType(keyEntity
                            .getAlgorithm()
                            .getAlgorithmName())
                    .firstName(userFirstName)
                    .lastName(userLastName)
                    .patronymic(userPatronymic)
                    .build();
        }

    }

    @Override
    public SesVerifyJsonDto verifyUnqualifiedSignature(@NotNull MultipartFile container) {
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


    private List<ContainerSelector> generateContainerSelectors() {
        val firstName = createContainerSelector(ContainerSelectorCode.USERFIRSTNAME);
        val lastName = createContainerSelector(ContainerSelectorCode.USERLASTNAME);
        val patronymic = createContainerSelector(ContainerSelectorCode.USERPATRONYMIC);
        val userLogin = createContainerSelector(ContainerSelectorCode.USERLOGIN);
        val timestamp = createContainerSelector(ContainerSelectorCode.TIMESTAMP);
        val sourceFile = createContainerSelector(ContainerSelectorCode.SOURCEFILE);
        val detachedSign = createContainerSelector(ContainerSelectorCode.DETACHEDSIGN);
        val detachedPub = createContainerSelector(ContainerSelectorCode.DETACHEDPUB);
        val signatureAlgorithm = createContainerSelector(ContainerSelectorCode.SIGNATUREALGORITHM);


        return List.of(firstName, lastName, patronymic, userLogin, timestamp, sourceFile, detachedSign, detachedPub,
                signatureAlgorithm);
    }

    private ContainerSelector createContainerSelector(ContainerSelectorCode containerSelectorCode) {
        val selector = new ContainerSelector();
        selector.setCode(containerSelectorCode);
        return selector;
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

        SesVerifyJsonDto sesVerifyJsonDto = new SesVerifyJsonDto();

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

        return sesVerifyJsonDto;
    }
}
