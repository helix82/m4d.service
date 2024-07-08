package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.api.model.ContainerPart;
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.api.model.ContainerSelectorCode;
import su.opencode.digsigcik.synapi.dto.ContainerUserInfoDto;
import su.opencode.digsigcik.synapi.dto.SesUserInfoDto;
import su.opencode.digsigcik.synapi.dto.SesVerifyJsonDto;
import su.opencode.digsigcik.synapi.exception.SesException;
import su.opencode.digsigcik.synapi.exception.SesVerifyException;
import su.opencode.digsigcik.synapi.model.PinCode;
import su.opencode.digsigcik.synapi.model.UserProjection;
import su.opencode.digsigcik.synapi.repository.PinCodeRepository;
import su.opencode.digsigcik.synapi.repository.UserProjectionRepository;
import su.opencode.digsigcik.synapi.util.FileUtils;
import su.opencode.digsigcik.synapi.util.HashUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static su.opencode.digsigcik.synapi.util.Constant.*;
import static su.opencode.digsigcik.synapi.util.HashUtils.generateHash;

@Service
@RequiredArgsConstructor
public class SimpleSignatureServiceImpl implements SimpleSignatureService {
    private static final String SIGNATURE_PUBLIC_KEY_SEPARATOR = "---";
    private final ContainerService containerService;
    private final PinCodeRepository pinCodeRepository;
    private final UserProjectionRepository userProjectionRepository;

    @Override
    public Resource createSimpleCustomSignatureResource(@NotNull MultipartFile srcFile,
                                                        @NotNull ContainerUserInfoDto containerUserInfo) {
        UserProjection userProjection = userProjectionRepository.getUserProjectionByLogin(containerUserInfo.getLogin());
        if (userProjection == null) {
            throw new SesException(USER_PROJECTION_NOT_FOUND);
        }
        PinCode pinCode1 = pinCodeRepository.getPinCodeByUserProjection(userProjection);
        containerUserInfo.setPinCodeHashAlgorithm(pinCode1.getAlgorithm()
                .getAlgorithmName());
        String pinCodeHash = generateHash(containerUserInfo.getPinCode()
                .getBytes(StandardCharsets.UTF_8), pinCode1.getAlgorithm());
        if (!pinCode1.getPinCodeHash()
                .equals(pinCodeHash)) {
            throw new SesException(PIN_CODE_ERROR_MESSAGE);
        }


        String operationDateHash = generateHash(containerUserInfo.getTimestamp()
                .toString()
                .getBytes(), pinCode1.getAlgorithm());
        String fileHash;
        try {
            fileHash = generateHash(srcFile.getBytes(), pinCode1.getAlgorithm());
        } catch (IOException e) {
            throw new SesException(EMPTY_BYTE_ARRAY_ERROR, e.getCause());
        }
        String signature = createCustomSimpleSignature(pinCodeHash, fileHash, operationDateHash);

        String sourceDirectoryPathName = String.format("%s%s", ROOT_FILE_STORAGE_PATH, SOURCE_DIRECTORY_PATH);
        String sigFilePathName = String.format("%s%s", sourceDirectoryPathName, "SHA256sig.sig");
        FileUtils.createDirectoryIfSheNotExist(sourceDirectoryPathName);
        FileUtils.createFile(sigFilePathName, signature.getBytes());

        MultipartFile sigMultipartFile = FileUtils.mapFileToMultipart(sigFilePathName);
        File sigFile = new File(sigFilePathName);
        if (sigFile.exists()) {
            FileUtils.deleteFile(sigFilePathName);
        }
        return containerService.createPdfContainer(srcFile, sigMultipartFile, containerUserInfo);
    }


    @Override
    public SesVerifyJsonDto verifyCustomSimpleSignature(@NotNull MultipartFile container) {
        List<ContainerSelector> containerSelectors =
                new ArrayList<>(List.of(createContainerSelector(ContainerSelectorCode.USERFIRSTNAME),
                        createContainerSelector(ContainerSelectorCode.USERLASTNAME),
                        createContainerSelector(ContainerSelectorCode.USERPATRONYMIC),
                        createContainerSelector(ContainerSelectorCode.USERLOGIN),
                        createContainerSelector(ContainerSelectorCode.TIMESTAMP),
                        createContainerSelector(ContainerSelectorCode.SOURCEFILE),
                        createContainerSelector(ContainerSelectorCode.DETACHEDSIGN),
                        createContainerSelector(ContainerSelectorCode.HASHTYPE)
                ));
        try {
            Map<String, ContainerPart> containerData = containerService.getContainerData(container,
                    containerSelectors, false);

            if (containerData == null) {
                throw new SesVerifyException("Verify ses error!");
            }
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
            String hashType = new String(containerData.get("hashType")
                    .getData(), StandardCharsets.UTF_8);
            byte[] srcFile = containerData.get("sourceFile")
                    .getData();

            UserProjection userProjection = userProjectionRepository.getUserProjectionByLogin(userLogin);
            PinCode pinCode = pinCodeRepository.getPinCodeByUserProjection(userProjection);
            if (userProjection == null) {
                throw new SesVerifyException(USER_PROJECTION_NOT_FOUND);
            }

            SesVerifyJsonDto sesVerifyJsonDto = new SesVerifyJsonDto();

            if (userProjection.getLogin()
                    .equals(userLogin)) {
                String fileHash = HashUtils.generateHash(srcFile, pinCode.getAlgorithm());
                String operationDateHash = generateHash(timestamp.getBytes(), pinCode.getAlgorithm());
                String signature = createCustomSimpleSignature(pinCode.getPinCodeHash(), fileHash, operationDateHash);

                if (userSignature.equals(signature)) {
                    sesVerifyJsonDto.setValid(true);

                } else {
                    sesVerifyJsonDto.setValid(false);
                }
                sesVerifyJsonDto.setUserInfo(SesUserInfoDto.builder()
                        .hashAlgorithmType(hashType)
                        .hashSignatureAlgorithm("CUSTOM")
                        .pinCodeHashAlgorithm(pinCode.getAlgorithm()
                                .getAlgorithmName())
                        .firstname(userFirstname)
                        .lastname(userLastname)
                        .patronymic(userPatronymic)
                        .timestamp(LocalDateTime.parse(timestamp))
                        .build());
            }

            return sesVerifyJsonDto;
        } catch (Exception e) {
            SesVerifyJsonDto sesVerifyJsonDto = new SesVerifyJsonDto();
            sesVerifyJsonDto.setValid(false);
            sesVerifyJsonDto.setVerifyResultMessage(e.getMessage());
            return sesVerifyJsonDto;
        }
    }

    @Override
    public Resource createSimpleSignatureResource(@NotNull MultipartFile srcFile,
                                                  @NotNull ContainerUserInfoDto containerUserInfo) throws IOException {

        byte[] signature = createSignature(srcFile.getBytes(), containerUserInfo.getPrivateKey(),containerUserInfo.getTimestamp().toString());

        String sourceDirectoryPathName = String.format("%s%s", ROOT_FILE_STORAGE_PATH, SOURCE_DIRECTORY_PATH);
        String sigFilePathName = String.format("%s%s", sourceDirectoryPathName, "temp.sig");
        FileUtils.createDirectoryIfSheNotExist(sourceDirectoryPathName);
        FileUtils.createFile(sigFilePathName,combineSignatureAndPublicKey(signature,containerUserInfo.getPublicKey()
                .getEncoded()));

        MultipartFile sigMultipartFile = FileUtils.mapFileToMultipart(sigFilePathName);

        File sigFile = new File(sigFilePathName);
        if (sigFile.exists()) {
            FileUtils.deleteFile(sigFilePathName);
        }

        return containerService.createPdfContainer(srcFile, sigMultipartFile, containerUserInfo);
    }


    @Override
    public SesVerifyJsonDto verifySimpleSignature(@NotNull MultipartFile container) {

        List<ContainerSelector> containerSelectors =
                new ArrayList<>(List.of(createContainerSelector(ContainerSelectorCode.USERFIRSTNAME),
                        createContainerSelector(ContainerSelectorCode.USERLASTNAME),
                        createContainerSelector(ContainerSelectorCode.USERPATRONYMIC),
                        createContainerSelector(ContainerSelectorCode.USERLOGIN),
                        createContainerSelector(ContainerSelectorCode.TIMESTAMP),
                        createContainerSelector(ContainerSelectorCode.SOURCEFILE),
                        createContainerSelector(ContainerSelectorCode.DETACHEDSIGN),
                        createContainerSelector(ContainerSelectorCode.SIGNATUREALGORITHM),
                        createContainerSelector(ContainerSelectorCode.HASHTYPE)));
        try {
            Map<String, ContainerPart> containerData = containerService.getContainerData(container, containerSelectors,
                    false);

            if (containerData == null) {
                throw new SesVerifyException("Verify ses error!");
            }

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
            String signatureAndPublicKey = new String(containerData.get("detachedSign")
                    .getData(), StandardCharsets.UTF_8);
            String signatureAlgorithm = new String(containerData.get("signatureAlgorithm")
                    .getData(), StandardCharsets.UTF_8);
            String hashType = new String(containerData.get("hashType")
                    .getData(), StandardCharsets.UTF_8);
            byte[] srcFile = containerData.get("sourceFile")
                    .getData();
            String[] parts = signatureAndPublicKey.split(SIGNATURE_PUBLIC_KEY_SEPARATOR);
            byte[] signature = parts[0].getBytes();
            byte[] publicKeyBytes = parts[1].getBytes();
            SesVerifyJsonDto sesVerifyJsonDto = new SesVerifyJsonDto();

            // декодируем открытый ключ из Base64 в байты
            byte[] decodedPublicKey = Base64.getDecoder()
                    .decode(publicKeyBytes);

            // создаём спецификацию ключа из байтов декодированного ключа
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedPublicKey);

            // создаём фабрику ключей
            KeyFactory keyFactory = KeyFactory.getInstance(getKeyAlgorithm(signatureAlgorithm));

            // генерируем открытый ключ из спецификации ключа
            PublicKey publicKey1 = keyFactory.generatePublic(keySpec);
            boolean valid = verifySignature(srcFile, Base64.getDecoder()
                    .decode(signature), publicKey1, timestamp);

            if (valid) {
                sesVerifyJsonDto.setValid(true);
            } else {
                sesVerifyJsonDto.setValid(false);
            }
            sesVerifyJsonDto.setUserInfo(SesUserInfoDto.builder()
                    .hashAlgorithmType(hashType)
                    .hashSignatureAlgorithm(signatureAlgorithm)
//                    .pinCodeHashAlgorithm(pinCode.getAlgorithm()
//                            .getAlgorithmName())
                    .firstname(userFirstname)
                    .lastname(userLastname)
                    .patronymic(userPatronymic)
                    .timestamp(LocalDateTime.parse(timestamp))
                    .build());
            return sesVerifyJsonDto;
        } catch (Exception e) {
            SesVerifyJsonDto sesVerifyJsonDto = new SesVerifyJsonDto();
            sesVerifyJsonDto.setValid(false);
            sesVerifyJsonDto.setVerifyResultMessage(e.getMessage());
            return sesVerifyJsonDto;
        }
    }

    private static String createCustomSimpleSignature(String pinCodeHash, String fileHash, String operationDate) {
        return String.format("%s.%s.%s", pinCodeHash, fileHash, operationDate);
    }

    private static byte[]  combineSignatureAndPublicKey(byte[] signature, byte[] publicKey) {
        return (Base64.getEncoder()
                .encodeToString(signature) + SIGNATURE_PUBLIC_KEY_SEPARATOR + Base64.getEncoder()
                .encodeToString(publicKey)).getBytes();
    }

    private static ContainerSelector createContainerSelector(ContainerSelectorCode containerSelectorCode) {
        val selector = new ContainerSelector();
        selector.setCode(containerSelectorCode);
        return selector;
    }

    @Override
    public PrivateKey getPrivateKey(byte[] privateKeyBytes, String pinCode, String algorithm) {
        try {
            byte[] decryptedPrivateKeyBytes = decryptPrivateKey(privateKeyBytes, pinCode, algorithm);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decryptedPrivateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(getKeyAlgorithm(algorithm));
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public PrivateKey getPrivateKey(byte[] privateKeyBytes, String algorithm) {
        try {
            byte[] decryptedPrivateKeyBytes = decryptPrivateKey(privateKeyBytes, algorithm);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decryptedPrivateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(getKeyAlgorithm(algorithm));
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public PublicKey getPublicKey(byte[] publicKeyBytes, String algorithm) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(getKeyAlgorithm(algorithm));
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] createSignature(byte[] fileContent, PrivateKey privateKey, String timestamp) {
        try {
            Signature signature = Signature.getInstance("SHA256with" + privateKey.getAlgorithm());
            signature.initSign(privateKey);
            signature.update(fileContent);
            signature.update(timestamp.getBytes());
            return signature.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean verifySignature(byte[] fileContent, byte[] signatureBytes, PublicKey publicKey, String timestamp) {
        try {
            Signature verificationSignature = Signature.getInstance("SHA256with" + publicKey.getAlgorithm());
            verificationSignature.initVerify(publicKey);
            verificationSignature.update(fileContent);
            verificationSignature.update(timestamp.getBytes());
            return verificationSignature.verify(signatureBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getKeyAlgorithm(String signatureAlgorithm) {
        String keyAlgorithm = signatureAlgorithm;
        if (keyAlgorithm.endsWith("RSA")) {
            keyAlgorithm = "RSA";
        } else if (keyAlgorithm.endsWith("ECDSA")) {
            keyAlgorithm = "EC";
        } else if (keyAlgorithm.endsWith("DSA")) {
            keyAlgorithm = "DSA";
        }
        return keyAlgorithm;
    }

    private byte[] decryptPrivateKey(byte[] privateKeyBytes, String pinCode, String algorithm) throws InvalidKeyException {
        // Implement private key decryption using the provided pin code and algorithm
        return privateKeyBytes;
    }

    private byte[] decryptPrivateKey(byte[] privateKeyBytes, String algorithm) throws InvalidKeyException {
        // Implement private key decryption using the provided pin code and algorithm
        return privateKeyBytes;
    }
}
