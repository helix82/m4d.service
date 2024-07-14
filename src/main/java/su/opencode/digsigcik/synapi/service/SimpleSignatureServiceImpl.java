package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.ContainerInputInfoDto;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.PinCode;
import su.opencode.digsigcik.synapi.model.UserProjection;
import su.opencode.digsigcik.synapi.repository.PinCodeRepository;
import su.opencode.digsigcik.synapi.repository.UserProjectionRepository;
import su.opencode.digsigcik.synapi.util.Constants;
import su.opencode.digsigcik.synapi.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static su.opencode.digsigcik.synapi.util.Constants.SIGNATURE_PUBLIC_KEY_SEPARATOR;
import static su.opencode.digsigcik.synapi.util.HashUtils.generateHash;

@Service
@RequiredArgsConstructor
public class SimpleSignatureServiceImpl implements SimpleSignatureService {

    private final ContainerService containerService;
    private final PinCodeRepository pinCodeRepository;
    private final UserProjectionRepository userProjectionRepository;

    @Override
    public Resource createSimpleCustomSignatureResource(@NotNull MultipartFile srcFile,
                                                        @NotNull ContainerInputInfoDto containerInputInfo) {
        UserProjection userProjection =
                userProjectionRepository.getUserProjectionByLogin(containerInputInfo.getLogin());
        if (userProjection == null) {
            throw new BadRequestException(Constants.USER_PROJECTION_NOT_FOUND);
        }

        PinCode pinCode = pinCodeRepository.getPinCodeByUserProjection(userProjection);
        containerInputInfo.setPinCodeHashAlgorithm(pinCode.getAlgorithm()
                .getAlgorithmName());

        String pinCodeHash = generateHash(containerInputInfo.getPinCode()
                .getBytes(StandardCharsets.UTF_8), pinCode.getAlgorithm());
        if (!pinCode.getPinCodeHash()
                .equals(pinCodeHash)) {
            throw new BadRequestException(Constants.PIN_CODE_ERROR_MESSAGE);
        }

        String operationDateHash = generateHash(containerInputInfo.getTimestamp()
                .toString()
                .getBytes(), pinCode.getAlgorithm());
        String fileHash;
        try {
            fileHash = generateHash(srcFile.getBytes(), pinCode.getAlgorithm());
        } catch (IOException e) {
            throw new BadRequestException(Constants.EMPTY_BYTE_ARRAY_ERROR, e.getCause());
        }

        String signature = createCustomSimpleSignature(pinCodeHash, fileHash, operationDateHash);
        String sigFilePathName = createSignatureFile(signature.getBytes(), "SHA256sig.sig");

        MultipartFile sigMultipartFile = FileUtils.mapFileToMultipart(sigFilePathName);
        deleteFileIfExists(sigFilePathName);

        return containerService.createContainer(srcFile, sigMultipartFile, containerInputInfo);
    }

    @Override
    public Resource createSimpleSignatureResource(@NotNull MultipartFile srcFile,
                                                  @NotNull ContainerInputInfoDto containerInputInfo) throws IOException {
        byte[] signature = createSignature(srcFile.getBytes(), containerInputInfo.getPrivateKey(),
                containerInputInfo.getTimestamp()
                .toString());
        String sigFilePathName = createSignatureFile(combineSignatureAndPublicKey(signature,
                containerInputInfo.getPublicKey()
                .getEncoded()), "temp.sig");

        MultipartFile sigMultipartFile = FileUtils.mapFileToMultipart(sigFilePathName);
        deleteFileIfExists(sigFilePathName);

        return containerService.createContainer(srcFile, sigMultipartFile, containerInputInfo);
    }

    private String createSignatureFile(byte[] signature, String fileName) {
        String sourceDirectoryPathName = String.format("%s%s", Constants.ROOT_FILE_STORAGE_PATH,
                Constants.SOURCE_DIRECTORY_PATH);
        String sigFilePathName = String.format("%s%s", sourceDirectoryPathName, fileName);
        FileUtils.createDirectoryIfSheNotExist(sourceDirectoryPathName);
        FileUtils.createFile(sigFilePathName, signature);
        return sigFilePathName;
    }

    private void deleteFileIfExists(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            FileUtils.deleteFile(filePath);
        }
    }

    static String createCustomSimpleSignature(String pinCodeHash, String fileHash, String operationDate) {
        return String.format("%s.%s.%s", pinCodeHash, fileHash, operationDate);
    }

    private static byte[] combineSignatureAndPublicKey(byte[] signature, byte[] publicKey) {
        return (Base64.getEncoder()
                .encodeToString(signature) + SIGNATURE_PUBLIC_KEY_SEPARATOR + Base64.getEncoder()
                .encodeToString(publicKey)).getBytes();
    }

    @Override
    public PrivateKey getPrivateKey(byte[] privateKeyBytes, String pinCode, String algorithm) {
        return getPrivateKey(privateKeyBytes, pinCode, algorithm, true);
    }

    @Override
    public PrivateKey getPrivateKey(byte[] privateKeyBytes, String algorithm) {
        return getPrivateKey(privateKeyBytes, null, algorithm, false);
    }

    private PrivateKey getPrivateKey(byte[] privateKeyBytes, String pinCode, String algorithm, boolean usePinCode) {
        try {
            byte[] decryptedPrivateKeyBytes = usePinCode ? decryptPrivateKey(privateKeyBytes, pinCode, algorithm) :
                    decryptPrivateKey(privateKeyBytes, algorithm);
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

    public String getKeyAlgorithm(String signatureAlgorithm) {
        if (signatureAlgorithm.endsWith("RSA")) {
            return "RSA";
        } else if (signatureAlgorithm.endsWith("ECDSA")) {
            return "EC";
        } else if (signatureAlgorithm.endsWith("DSA")) {
            return "DSA";
        }
        return signatureAlgorithm;
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
