package su.opencode.digsigcik.synapi.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.api.model.ContainerPart;
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.client.RestTemplateClient;
import su.opencode.digsigcik.synapi.dscp.ContainerSelectorCode;
import su.opencode.digsigcik.synapi.dscp.SignatureTypeEnum;
import su.opencode.digsigcik.synapi.dto.ReportContainerDTO;
import su.opencode.digsigcik.synapi.dto.ReportDTO;
import su.opencode.digsigcik.synapi.enums.SignatureAlgorithm;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.PinCode;
import su.opencode.digsigcik.synapi.model.UserProjection;
import su.opencode.digsigcik.synapi.repository.PinCodeRepository;
import su.opencode.digsigcik.synapi.repository.UserProjectionRepository;
import su.opencode.digsigcik.synapi.util.HashUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import static su.opencode.digsigcik.synapi.service.SimpleSignatureServiceImpl.createCustomSimpleSignature;
import static su.opencode.digsigcik.synapi.util.Constants.SIGNATURE_PUBLIC_KEY_SEPARATOR;
import static su.opencode.digsigcik.synapi.util.HashUtils.generateHash;

@Service
public class VerificationServiceImpl implements VerificationService {

    @Value("${cryptopro.svs.uri}")
    private String svc_uri;

    private RestTemplateClient restTemplateClient;
    private final ContainerService containerService;
    private final PinCodeRepository pinCodeRepository;
    private final UserProjectionRepository userProjectionRepository;

    public VerificationServiceImpl(ContainerService containerService, PinCodeRepository pinCodeRepository,
                                   UserProjectionRepository userProjectionRepository) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, NoSuchProviderException, KeyManagementException {
        this.containerService = containerService;
        this.pinCodeRepository = pinCodeRepository;
        this.userProjectionRepository = userProjectionRepository;
        this.restTemplateClient = new RestTemplateClient(true);
    }

    public ReportContainerDTO verifyDetached(@NotNull MultipartFile sourceFile,
                                             @NotNull MultipartFile detachedSign) throws IOException, URISyntaxException {
        resetAll(svc_uri);
        String id1 = uploadDocument(svc_uri, sourceFile.getBytes(), sourceFile.getOriginalFilename()).get("id")
                .getAsString();
        String id2 = uploadDocument(svc_uri, detachedSign.getBytes(), detachedSign.getOriginalFilename()).get("id")
                .getAsString();
        String metaJsonStr = verifyDetachedSignature(svc_uri, id1, id2);
        byte[] reportFile = getReportFile(svc_uri);
        resetAll(svc_uri);
        JSONObject json = new JSONObject(metaJsonStr);
        byte[] metaJsonContent = json.toString(4).getBytes(StandardCharsets.UTF_8);
        return ReportContainerDTO.builder()
                .signCheck(metaJsonContent)
                .signReport(reportFile)
                .build();
    }

    public ReportContainerDTO verifyAttached(@NotNull MultipartFile attachedSign) throws IOException, URISyntaxException {
        resetAll(svc_uri);
        String id = uploadDocument(svc_uri, attachedSign.getBytes(), attachedSign.getOriginalFilename()).get("id")
                .getAsString();
        String metaJsonStr = verifyAttachedSignature(svc_uri, id);
        byte[] reportFile = getReportFile(svc_uri);
        resetAll(svc_uri);
        JSONObject json = new JSONObject(metaJsonStr);
        byte[] metaJsonContent = json.toString(4).getBytes(StandardCharsets.UTF_8);
        return ReportContainerDTO.builder()
                .signCheck(metaJsonContent)
                .signReport(reportFile)
                .build();
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
                    .Error("Не удалось определить тип подписи.")
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
            return verifyDetachedSignature(srcFile, detachedSign, sourceFileFullName);
        } else if (attachedSign.length > 0) {
            return verifyAttachedSignature(attachedSign, sourceFileFullName);
        } else {
            throw new IllegalArgumentException("Both detachedSign and attachedSign are empty");
        }
    }

    private byte[] getByteArrayData(Map<String, ContainerPart> containerData, String key) {
        if (containerData.containsKey(key)) {
            return containerData.get(key).getData();
        }
        return new byte[0];
    }

    private ReportDTO verifyAttachedSignature(byte[] srcFile, String sourceFileFullName) throws Exception {
        resetAll(svc_uri);
        String id1 = uploadDocument(svc_uri, srcFile, sourceFileFullName+".sig").get("id")
                .getAsString();
        String result = verifyAttachedSignature(svc_uri, id1);
        resetAll(svc_uri);
        Gson gson = new Gson();
        return gson.fromJson(result, ReportDTO.class);
    }

    private ReportDTO verifyDetachedSignature(byte[] srcFile, byte[] detachedSign, String sourceFileFullName) throws Exception {
        resetAll(svc_uri);
        String id1 = uploadDocument(svc_uri, srcFile, sourceFileFullName).get("id")
                .getAsString();
        String id2 = uploadDocument(svc_uri, detachedSign, sourceFileFullName + ".sig").get("id")
                .getAsString();
        String result = verifyDetachedSignature(svc_uri, id1, id2);
        resetAll(svc_uri);
        Gson gson = new Gson();
        return gson.fromJson(result, ReportDTO.class);
    }

    private ReportDTO handleCustomSimpleSignature(Map<String, ContainerPart> containerData) {
        String userLogin = getStringData(containerData, "userLogin");
        byte[] srcFile = containerData.get("sourceFile")
                .getData();
        byte[] detachedSign = containerData.get("detachedSign")
                .getData();
        String timestamp = getStringData(containerData, "timestamp");
        String hashType = getStringData(containerData, "hashType");
        String userFirstname = getStringData(containerData, "userFirstname");
        String userLastname = getStringData(containerData, "userLastname");
        String userPatronymic = getStringData(containerData, "userPatronymic");

        UserProjection userProjection = userProjectionRepository.getUserProjectionByLogin(userLogin);
        PinCode pinCode = pinCodeRepository.getPinCodeByUserProjection(userProjection);
        if (userProjection == null) {
            return ReportDTO.builder()
                    .Error("Не удалось получить информацию о пользователе")
                    .ErrorDescription("Не удалось получить информацию о пользователе: " + userPatronymic + " " + userFirstname + " " + userLastname)
                    .build();
        }

        if (userProjection.getLogin()
                .equals(userLogin)) {
            boolean valid = verifyCustomSimpleSignature(srcFile,detachedSign,pinCode,timestamp);

            if (valid) {
                return ReportDTO.builder()
                        .Message("Простая подпись валидна")
                        .MessageDetail("Простая подпись пользователя " + userPatronymic + " " + userFirstname + " " + userLastname + " валидна. Дата подписи: " + timestamp)
                        .build();
            } else {
                return ReportDTO.builder()
                        .Message("Простая подпись не валидна")
                        .MessageDetail("Простая подпись пользователя " + userPatronymic + " " + userFirstname + " " + userLastname + " НЕ валидна. Дата подписи: " + timestamp)
                        .build();
            }
        }

        return ReportDTO.builder()
                .Error("Не удалось определить тип подписи.")
                .build();
    }

    private ReportDTO handleSimpleSignature(Map<String, ContainerPart> containerData,
                                                               String signatureAlgorithm) throws Exception {
        byte[] srcFile = containerData.get("sourceFile")
                .getData();
        byte[] detachedSign = containerData.get("detachedSign")
                .getData();
        String timestamp = getStringData(containerData, "timestamp");
        String hashType = getStringData(containerData, "hashType");
        String userFirstname = getStringData(containerData, "userFirstname");
        String userLastname = getStringData(containerData, "userLastname");
        String userPatronymic = getStringData(containerData, "userPatronymic");

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
            return ReportDTO.builder()
                    .Message("Простая подпись валидна")
                    .MessageDetail("Простая подпись пользователя " + userPatronymic + " " + userFirstname + " " + userLastname + " валидна. Дата подписи: " + timestamp)
                    .build();
        } else {
            return ReportDTO.builder()
                    .Message("Простая подпись не валидна")
                    .MessageDetail("Простая подпись пользователя " + userPatronymic + " " + userFirstname + " " + userLastname + " НЕ валидна. Дата подписи: " + timestamp)
                    .build();
        }
    }

    private JsonObject uploadDocument(String uri, byte[] file, String filename) throws URISyntaxException, IOException {
        RestTemplateClient.RestTemplateClientRequest restTemplateClientRequest =
                RestTemplateClient.RestTemplateClientRequest.builder()
                        .httpMethod(HttpMethod.POST)
                        .requestUri(new URI(uri + "/documents/"))
                        .bytes(file)
                        .fileName(filename)
                        .build();
        return restTemplateClient.requestJson(restTemplateClientRequest);
    }

    private JsonObject uploadDocument(String uri, MultipartFile file) throws URISyntaxException, IOException {
        RestTemplateClient.RestTemplateClientRequest restTemplateClientRequest =
                RestTemplateClient.RestTemplateClientRequest.builder()
                        .httpMethod(HttpMethod.POST)
                        .requestUri(new URI(uri + "/documents/"))
                        .bytes(file.getBytes())
                        .fileName(file.getOriginalFilename())
                        .build();
        return restTemplateClient.requestJson(restTemplateClientRequest);
    }

    private String listUploadedDocuments(String uri) throws URISyntaxException,
            IOException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        headers.put("Accept", "application/json");
        RestTemplateClient.RestTemplateClientRequest restTemplateClientRequest =
                RestTemplateClient.RestTemplateClientRequest.builder()
                        .httpMethod(HttpMethod.GET)
                        .requestUri(new URI(uri + "/documents/"))
                        .httpHeader(headers)
                        .build();
        return restTemplateClient.requestString(restTemplateClientRequest);
    }
    private byte [] getReportFile(String uri) throws
            IOException {
        return restTemplateClient.downloadFile(uri + "/signatures/report/print");
    }

    private String verifyAttachedSignature(String uri, String id) throws URISyntaxException,
            IOException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        headers.put("Accept", "application/json");
        String requestContent = "{\"DocumentIds\":[\"" + id + "\"],\"CustomParams\":{\"CertVerifiersPluginsIds\":[]}}";
        RestTemplateClient.RestTemplateClientRequest restTemplateClientRequest =
                RestTemplateClient.RestTemplateClientRequest.builder()
                        .httpMethod(HttpMethod.POST)
                        .requestUri(new URI(uri + "/signatures/package/session"))
                        .httpHeader(headers)
                        .requestContent(requestContent)
                        .build();
        return restTemplateClient.requestString(restTemplateClientRequest);
    }

    private String verifyDetachedSignature(String uri, String id1, String id2) throws URISyntaxException,
            IOException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        headers.put("Accept", "application/json");
        String requestContent = "{\"DocumentIds\":[\"" + id1 + "\",\"" + id2 + "\"],\"CustomParams" +
                "\":{\"CertVerifiersPluginsIds\":[]}}";
        RestTemplateClient.RestTemplateClientRequest restTemplateClientRequest =
                RestTemplateClient.RestTemplateClientRequest.builder()
                        .httpMethod(HttpMethod.POST)
                        .requestUri(new URI(uri + "/signatures/package/session"))
                        .httpHeader(headers)
                        .requestContent(requestContent)
                        .build();
        return restTemplateClient.requestString(restTemplateClientRequest);
    }

    public boolean verifySimpleSignature(byte[] fileContent, byte[] signatureBytes, PublicKey publicKey, String timestamp) {
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

    public boolean verifyCustomSimpleSignature(byte[] srcFile, byte[] detachedSign,  PinCode pinCode, String timestamp) {
        String fileHash = HashUtils.generateHash(srcFile, pinCode.getAlgorithm());
        String operationDateHash = generateHash(timestamp.getBytes(), pinCode.getAlgorithm());
        String signature = createCustomSimpleSignature(pinCode.getPinCodeHash(), fileHash, operationDateHash);
        return  new String(detachedSign, StandardCharsets.UTF_8).equals(signature);
    }

    private void resetAll(String uri) throws URISyntaxException, IOException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        RestTemplateClient.RestTemplateClientRequest restTemplateClientRequest =
                RestTemplateClient.RestTemplateClientRequest.builder()
                        .httpMethod(HttpMethod.POST)
                        .requestUri(new URI(uri + "/documents/reset/all"))
                        .requestContent("")
                        .httpHeader(headers)
                        .build();
        restTemplateClient.requestString(restTemplateClientRequest);
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
