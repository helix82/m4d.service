package su.opencode.digsigcik.synapi.service;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.api.model.ContainerPart;
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.client.RestTemplateClient;
import su.opencode.digsigcik.synapi.dscp.ContainerSelectorCode;
import su.opencode.digsigcik.synapi.dscp.SignatureTypeEnum;
import su.opencode.digsigcik.synapi.dto.ReportDTO;
import su.opencode.digsigcik.synapi.enums.SignatureAlgorithm;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.PinCode;
import su.opencode.digsigcik.synapi.model.UserProjection;
import su.opencode.digsigcik.synapi.repository.PinCodeRepository;
import su.opencode.digsigcik.synapi.repository.UserProjectionRepository;
import su.opencode.digsigcik.synapi.util.HashUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.*;

import static su.opencode.digsigcik.synapi.service.SimpleSignatureServiceImpl.createCustomSimpleSignature;
import static su.opencode.digsigcik.synapi.util.Constants.SIGNATURE_PUBLIC_KEY_SEPARATOR;
import static su.opencode.digsigcik.synapi.util.HashUtils.generateHash;

@Service
public class ContainerVerificationServiceImpl implements ContainerVerificationService {

    private RestTemplateClient restTemplateClient;
    private final ContainerService containerService;
    private final PinCodeRepository pinCodeRepository;
    private final UserProjectionRepository userProjectionRepository;
    private final SignatureVerificationService signatureVerificationService;

    public ContainerVerificationServiceImpl(ContainerService containerService, PinCodeRepository pinCodeRepository,
                                            UserProjectionRepository userProjectionRepository, @Qualifier("cryptopro-keylab") SignatureVerificationService signatureVerificationService) throws CertificateException, NoSuchAlgorithmException
            , KeyStoreException, IOException, NoSuchProviderException, KeyManagementException {
        this.containerService = containerService;
        this.pinCodeRepository = pinCodeRepository;
        this.userProjectionRepository = userProjectionRepository;
        this.signatureVerificationService = signatureVerificationService;
        this.restTemplateClient = new RestTemplateClient(true);
    }


    public ReportDTO verifyContainer(@NotNull MultipartFile container) {
        List<ContainerSelector> containerSelectors = createContainerSelectors();
        try {
            var containerData = containerService.getContainerData(container, containerSelectors, false);

            if (containerData == null && !containerData.isEmpty()) {
                throw new BadRequestException("Verify ses error!");
            }

            String signatureType = getStringData(containerData, "signatureType");
            String signatureAlgorithm = getStringData(containerData, "signatureAlgorithm");

            if (isQualifiedSignature(signatureType)) {
                return handleQualifiedSignature(containerData);
            } else if (isCustomSimpleSignature(signatureType, signatureAlgorithm)) {
                return handleCustomSimpleSignature(containerData);
            } else if (isSimpleSignature(signatureType, signatureAlgorithm)) {
                return handleSimpleSignature(containerData, signatureAlgorithm);
            }
            return ReportDTO.builder()
                    .error("Не удалось определить тип подписи.")
                    .build();
        } catch (Exception e) {
            throw new BadRequestException("Verify ses error!", e);
        }
    }

    private List<ContainerSelector> createContainerSelectors() {
        return new ArrayList<>(List.of(
                createContainerSelector(ContainerSelectorCode.USERFIRSTNAME),
                createContainerSelector(ContainerSelectorCode.USERLASTNAME),
                createContainerSelector(ContainerSelectorCode.USERPATRONYMIC),
                createContainerSelector(ContainerSelectorCode.USERLOGIN),
                createContainerSelector(ContainerSelectorCode.TIMESTAMP),
                createContainerSelector(ContainerSelectorCode.SOURCE_FILE),
                createContainerSelector(ContainerSelectorCode.DETACHEDSIGN),
                createContainerSelector(ContainerSelectorCode.ATTACHEDSIGN),
                createContainerSelector(ContainerSelectorCode.HASHTYPE),
                createContainerSelector(ContainerSelectorCode.SIGNATUREALGORITHM),
                createContainerSelector(ContainerSelectorCode.SIGNATURE_TYPE),
                createContainerSelector(ContainerSelectorCode.SOURCE_FILE_FULL_NAME)
        ));
    }

    private String getStringData(Map<String, ContainerPart> containerData,
                                 String key) {
        return new String(containerData.get(key)
                .getData(), StandardCharsets.UTF_8);
    }

    private boolean isQualifiedSignature(String signatureType) {
        return signatureType.equals(SignatureTypeEnum.UNQUALIFIED.getCode())
                || signatureType.equals(SignatureTypeEnum.QUALIFIED.getCode())
                || signatureType.equals(SignatureTypeEnum.UNKNOWN.getCode());
    }

    private boolean isCustomSimpleSignature(String signatureType, String signatureAlgorithm) {
        return signatureType.equals(SignatureTypeEnum.SIMPLE.getCode()) && signatureAlgorithm.equals("CUSTOM");
    }

    private boolean isSimpleSignature(String signatureType, String signatureAlgorithm) {
        return signatureType.equals(SignatureTypeEnum.SIMPLE.getCode())
                && Arrays.asList(SignatureAlgorithm.values())
                .contains(SignatureAlgorithm.valueOf(signatureAlgorithm));
    }

    private ReportDTO handleQualifiedSignature(Map<String, ContainerPart> containerData) throws Exception {
        byte[] detachedSign = getByteArrayData(containerData, "detachedSign");
        byte[] attachedSign = getByteArrayData(containerData, "attachedSign");
        byte[] srcFile = getByteArrayData(containerData, "sourceFile");
        String sourceFileFullName = getStringData(containerData, "sourceFileFullName");

        if (detachedSign.length > 0) {
            return signatureVerificationService.verifyDetached(srcFile, detachedSign, sourceFileFullName);
        } else if (attachedSign.length > 0) {
            return signatureVerificationService.verifyAttached(attachedSign, sourceFileFullName);
        } else {
            throw new IllegalArgumentException("Both detachedSign and attachedSign are empty");
        }
    }

    private byte[] getByteArrayData(Map<String, ContainerPart> containerData, String key) {
        if (containerData.containsKey(key)) {
            return containerData.get(key)
                    .getData();
        }
        return new byte[0];
    }

    private ReportDTO handleCustomSimpleSignature(Map<String, ContainerPart> containerData) {
        String userLogin = getStringData(containerData, "userLogin");
        byte[] srcFile = containerData.get("sourceFile")
                .getData();
        byte[] detachedSign = containerData.get("detachedSign")
                .getData();
        String timestamp = getStringData(containerData, "timestamp");
        String userFirstname = getStringData(containerData, "userFirstname");
        String userLastname = getStringData(containerData, "userLastname");
        String userPatronymic = getStringData(containerData, "userPatronymic");
        String sourceFileFullName = getStringData(containerData, "sourceFileFullName");


        UserProjection userProjection = userProjectionRepository.getUserProjectionByLogin(userLogin);
        PinCode pinCode = pinCodeRepository.getPinCodeByUserProjection(userProjection);
        if (userProjection == null) {
            ReportDTO.Result.VerificationResult verificationResult = ReportDTO.Result.VerificationResult.builder()
                    .result(false)
                    .build();
            ReportDTO.Result result = ReportDTO.Result.builder()
                    .originalDocumentName(sourceFileFullName)
                    .verificationTime(LocalDateTime.now()
                            .toString())
                    .verificationResults(new ArrayList<>(Collections.singletonList(verificationResult)))
                    .build();
            return ReportDTO.builder()
                    .results(new ArrayList<>(Collections.singletonList(result)))
                    .error("Подпись недействительна. Не удалось получить информацию о пользователе")
                    .errorDescription("Не удалось получить информацию о пользователе: " + userPatronymic + " " + userFirstname + " " + userLastname)
                    .build();
        }

        if (userProjection.getLogin()
                .equals(userLogin)) {
            boolean valid = verifyCustomSimpleSignature(srcFile, detachedSign, pinCode, timestamp);

            if (valid) {
                ReportDTO.Result.VerificationResult verificationResult = ReportDTO.Result.VerificationResult.builder()
                        .result(true)
                        .build();
                ReportDTO.Result result = ReportDTO.Result.builder()
                        .originalDocumentName(sourceFileFullName)
                        .verificationTime(LocalDateTime.now()
                                .toString())
                        .verificationResults(new ArrayList<>(Collections.singletonList(verificationResult)))
                        .build();
                return ReportDTO.builder()
                        .results(new ArrayList<>(Collections.singletonList(result)))
                        .message("Простая подпись действительна")
                        .messageDetail("Простая подпись пользователя " + userPatronymic + " " + userFirstname + " " + userLastname + " валидна. Дата подписи: " + timestamp)
                        .build();
            } else {
                ReportDTO.Result.VerificationResult verificationResult = ReportDTO.Result.VerificationResult.builder()
                        .result(false)
                        .build();
                ReportDTO.Result result = ReportDTO.Result.builder()
                        .originalDocumentName(sourceFileFullName)
                        .verificationTime(LocalDateTime.now()
                                .toString())
                        .verificationResults(new ArrayList<>(Collections.singletonList(verificationResult)))
                        .build();
                return ReportDTO.builder()
                        .results(new ArrayList<>(Collections.singletonList(result)))
                        .error("Подпись недействительна. Не удалось определить тип подписи.")
                        .errorDescription("Простая подпись пользователя " + userPatronymic + " " + userFirstname + " "
                                + userLastname + " НЕ валидна. Дата подписи: " + timestamp)
                        .build();
            }
        }
        ReportDTO.Result.VerificationResult verificationResult = ReportDTO.Result.VerificationResult.builder()
                .result(false)
                .build();
        ReportDTO.Result result = ReportDTO.Result.builder()
                .originalDocumentName(sourceFileFullName)
                .verificationTime(LocalDateTime.now()
                        .toString())
                .verificationResults(new ArrayList<>(Collections.singletonList(verificationResult)))
                .build();
        return ReportDTO.builder()
                .results(new ArrayList<>(Collections.singletonList(result)))
                .error("Подпись недействительна. Не удалось определить тип подписи.")
                .errorDescription("Не удалось определить тип подписи.")
                .build();
    }

    private ReportDTO handleSimpleSignature(Map<String, ContainerPart> containerData,
                                            String signatureAlgorithm) throws Exception {
        byte[] srcFile = containerData.get("sourceFile")
                .getData();
        byte[] detachedSign = containerData.get("detachedSign")
                .getData();
        String timestamp = getStringData(containerData, "timestamp");
        String userFirstname = getStringData(containerData, "userFirstname");
        String userLastname = getStringData(containerData, "userLastname");
        String userPatronymic = getStringData(containerData, "userPatronymic");
        String sourceFileFullName = getStringData(containerData, "sourceFileFullName");

        String[] parts = new String(detachedSign, StandardCharsets.UTF_8).split(SIGNATURE_PUBLIC_KEY_SEPARATOR);
        byte[] signature = parts[0].getBytes();
        byte[] publicKeyBytes = parts[1].getBytes();
        byte[] decodedPublicKey = Base64.getDecoder()
                .decode(publicKeyBytes);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedPublicKey);
        KeyFactory keyFactory = KeyFactory.getInstance(getKeyAlgorithm(signatureAlgorithm));
        PublicKey publicKey1 = keyFactory.generatePublic(keySpec);
        boolean valid = verifySimpleSignature(srcFile, Base64.getDecoder()
                .decode(signature), publicKey1, timestamp);
        if (valid) {
            ReportDTO.Result.VerificationResult verificationResult = ReportDTO.Result.VerificationResult.builder()
                    .result(true)
                    .build();
            ReportDTO.Result result = ReportDTO.Result.builder()
                    .originalDocumentName(sourceFileFullName)
                    .verificationTime(LocalDateTime.now()
                            .toString())
                    .verificationResults(new ArrayList<>(Collections.singletonList(verificationResult)))
                    .build();
            return ReportDTO.builder()
                    .results(new ArrayList<>(Collections.singletonList(result)))
                    .message("Простая подпись действительна")
                    .messageDetail("Простая подпись пользователя " + userPatronymic + " " + userFirstname + " " + userLastname + " валидна. Дата подписи: " + timestamp)
                    .build();
        } else {
            ReportDTO.Result.VerificationResult verificationResult = ReportDTO.Result.VerificationResult.builder()
                    .result(false)
                    .build();
            ReportDTO.Result result = ReportDTO.Result.builder()
                    .originalDocumentName(sourceFileFullName)
                    .verificationTime(LocalDateTime.now()
                            .toString())
                    .verificationResults(new ArrayList<>(Collections.singletonList(verificationResult)))
                    .build();
            return ReportDTO.builder()
                    .results(new ArrayList<>(Collections.singletonList(result)))
                    .error("Подпись недействительна.")
                    .errorDescription("Простая подпись пользователя " + userPatronymic + " " + userFirstname + " " + userLastname + " недействительна. Дата подписи: " + timestamp)
                    .build();
        }
    }

    public boolean verifySimpleSignature(byte[] fileContent, byte[] signatureBytes, PublicKey publicKey,
                                         String timestamp) {
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

    public boolean verifyCustomSimpleSignature(byte[] srcFile, byte[] detachedSign, PinCode pinCode, String timestamp) {
        String fileHash = HashUtils.generateHash(srcFile, pinCode.getAlgorithm());
        String operationDateHash = generateHash(timestamp.getBytes(), pinCode.getAlgorithm());
        String signature = createCustomSimpleSignature(pinCode.getPinCodeHash(), fileHash, operationDateHash);
        return new String(detachedSign, StandardCharsets.UTF_8).equals(signature);
    }


    private static ContainerSelector createContainerSelector(ContainerSelectorCode containerSelectorCode) {
        val selector = new ContainerSelector();
        selector.setCode(containerSelectorCode);
        return selector;
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
}
