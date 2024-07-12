package su.opencode.digsigcik.synapi.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.client.RestClient;
import su.opencode.digsigcik.synapi.client.RestMethod;
import su.opencode.digsigcik.synapi.client.RestRequest;
import su.opencode.digsigcik.synapi.dscp.ContainerSelectorCode;
import su.opencode.digsigcik.synapi.dto.SesUserInfoDto;
import su.opencode.digsigcik.synapi.dto.VerifyDto;
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
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import static su.opencode.digsigcik.synapi.service.SimpleSignatureServiceImpl.createCustomSimpleSignature;
import static su.opencode.digsigcik.synapi.util.Constants.USER_PROJECTION_NOT_FOUND;
import static su.opencode.digsigcik.synapi.util.Constants.SIGNATURE_PUBLIC_KEY_SEPARATOR;
import static su.opencode.digsigcik.synapi.util.HashUtils.generateHash;

@Service

public class VerificationServiceImpl implements VerificationService {

    @Value("${cryptopro.svs.uri}")
    private String svc_uri;

    private RestClient restClient;
    private final ContainerService containerService;
    private final PinCodeRepository pinCodeRepository;
    private final UserProjectionRepository userProjectionRepository;

    public VerificationServiceImpl(ContainerService containerService, PinCodeRepository pinCodeRepository,
                                   UserProjectionRepository userProjectionRepository) {
        this.containerService = containerService;
        this.pinCodeRepository = pinCodeRepository;
        this.userProjectionRepository = userProjectionRepository;
        //  this.restClient = new RestClient(false);
    }

    public VerifyDto verify(@NotNull MultipartFile container) {
        List<su.opencode.digsigcik.synapi.api.model.ContainerSelector> containerSelectors =
                new ArrayList<>(List.of(createContainerSelector(ContainerSelectorCode.USERFIRSTNAME),
                        createContainerSelector(ContainerSelectorCode.USERLASTNAME),
                        createContainerSelector(ContainerSelectorCode.USERPATRONYMIC),
                        createContainerSelector(ContainerSelectorCode.USERLOGIN),
                        createContainerSelector(ContainerSelectorCode.TIMESTAMP),
                        createContainerSelector(ContainerSelectorCode.SOURCE_FILE),
                        createContainerSelector(ContainerSelectorCode.DETACHEDSIGN),
                        createContainerSelector(ContainerSelectorCode.HASHTYPE),
                        createContainerSelector(ContainerSelectorCode.SIGNATUREALGORITHM)
                ));
        try {
            var containerData = containerService.getContainerData(container,
                    containerSelectors, false);

            if (containerData == null) {
                throw new BadRequestException("Verify ses error!");
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
            byte[] detachedSign = containerData.get("detachedSign")
                    .getData();
            String hashType = new String(containerData.get("hashType")
                    .getData(), StandardCharsets.UTF_8);
            byte[] srcFile = containerData.get("sourceFile")
                    .getData();
            String signatureAlgorithm = new String(containerData.get("signatureAlgorithm")
                    .getData(), StandardCharsets.UTF_8);
            VerifyDto verifyDto = new VerifyDto();
            if (signatureAlgorithm.equals("NOT IMPLEMENTED")) {
                this.restClient = new RestClient(false);
                if (detachedSign==null) {
                    resetAll(svc_uri);
                    String id1 = uploadDocument(svc_uri, srcFile).get("id")
                            .getAsString();
                    JsonObject result = verifyAttachedSignature(svc_uri, id1);
                    resetAll(svc_uri);
                    verifyDto.setValid(true);
                    verifyDto.setVerifyResultMessage(convertToString(result));
                    verifyDto.setUserInfo(SesUserInfoDto.builder()
                            .hashAlgorithmType(hashType)
                            .hashSignatureAlgorithm(signatureAlgorithm)
                            .firstname(userFirstname)
                            .lastname(userLastname)
                            .patronymic(userPatronymic)
                            .timestamp(LocalDateTime.parse(timestamp))
                            .build());
                    return verifyDto;
                } else {
                    resetAll(svc_uri);
                    String id1 = uploadDocument(svc_uri, srcFile).get("id")
                            .getAsString();
                    String id2 = uploadDocument(svc_uri, detachedSign).get("id")
                            .getAsString();
                    JsonObject result = verifyDetachedSignature(svc_uri, id1, id2);
                    resetAll(svc_uri);
                    verifyDto.setValid(true);
                    verifyDto.setVerifyResultMessage(convertToString(result));
                    verifyDto.setUserInfo(SesUserInfoDto.builder()
                            .hashAlgorithmType(hashType)
                            .hashSignatureAlgorithm(signatureAlgorithm)
                            .firstname(userFirstname)
                            .lastname(userLastname)
                            .patronymic(userPatronymic)
                            .timestamp(LocalDateTime.parse(timestamp))
                            .build());
                    return verifyDto;
                }

            } else if (signatureAlgorithm.equals("CUSTOM")) {
                UserProjection userProjection = userProjectionRepository.getUserProjectionByLogin(userLogin);
                PinCode pinCode = pinCodeRepository.getPinCodeByUserProjection(userProjection);
                if (userProjection == null) {
                    throw new BadRequestException(USER_PROJECTION_NOT_FOUND);
                }

                if (userProjection.getLogin()
                        .equals(userLogin)) {
                    String fileHash = HashUtils.generateHash(srcFile, pinCode.getAlgorithm());
                    String operationDateHash = generateHash(timestamp.getBytes(), pinCode.getAlgorithm());
                    String signature = createCustomSimpleSignature(pinCode.getPinCodeHash(), fileHash,
                            operationDateHash);

                    if (new String(detachedSign, StandardCharsets.UTF_8).equals(signature)) {
                        verifyDto.setValid(true);

                    } else {
                        verifyDto.setValid(false);
                    }
                    verifyDto.setUserInfo(SesUserInfoDto.builder()
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

                return verifyDto;
            } else if (Arrays.asList(SignatureAlgorithm.values())
                    .contains(SignatureAlgorithm.valueOf(signatureAlgorithm))) {
                String[] parts = new String(detachedSign, StandardCharsets.UTF_8).split(SIGNATURE_PUBLIC_KEY_SEPARATOR);
                byte[] signature = parts[0].getBytes();
                byte[] publicKeyBytes = parts[1].getBytes();

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
                    verifyDto.setValid(true);
                } else {
                    verifyDto.setValid(false);
                }
                verifyDto.setUserInfo(SesUserInfoDto.builder()
                        .hashAlgorithmType(hashType)
                        .hashSignatureAlgorithm(signatureAlgorithm)
                        .firstname(userFirstname)
                        .lastname(userLastname)
                        .patronymic(userPatronymic)
                        .timestamp(LocalDateTime.parse(timestamp))
                        .build());
                return verifyDto;
            }
            verifyDto.setValid(false);
            System.out.println(signatureAlgorithm);
            return verifyDto;

        } catch (Exception e) {
            throw new BadRequestException("Verify ses error!");
        }
    }

    private JsonObject uploadDocument(String uri, byte[] file) throws URISyntaxException, IOException {
        RestRequest restRequest = RestRequest.builder()
                .httpMethod(RestMethod.POST)
                .requestUri(new URI(uri + "/documents/"))
                .bytes(file)
                //     .fileName(file.getOriginalFilename())
                .build();
        return restClient.requestJson(restRequest);
    }

    private JsonObject uploadDocument(String uri, MultipartFile file) throws URISyntaxException, IOException {
        RestRequest restRequest = RestRequest.builder()
                .httpMethod(RestMethod.POST)
                .requestUri(new URI(uri + "/documents/"))
                .bytes(file.getBytes())
                .fileName(file.getOriginalFilename())
                .build();
        return restClient.requestJson(restRequest);
    }

    private String listUploadedDocuments(String uri) throws URISyntaxException,
            IOException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        headers.put("Accept", "application/json");
        RestRequest restRequest = RestRequest.builder()
                .httpMethod(RestMethod.GET)
                .requestUri(new URI(uri + "/documents/"))
                .httpHeader(headers)
                .build();
        return restClient.requestString(restRequest);
    }

    private JsonObject verifyAttachedSignature(String uri, String id) throws URISyntaxException,
            IOException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        headers.put("Accept", "application/json");
        String requestContent = "{\"DocumentIds\":[\"" + id + "\"],\"CustomParams\":{\"CertVerifiersPluginsIds\":[]}}";
        RestRequest restRequest = RestRequest.builder()
                .httpMethod(RestMethod.POST)
                .requestUri(new URI(uri + "/signatures/package/session"))
                .httpHeader(headers)
                .requestContent(requestContent)
                .build();
        return restClient.requestJson(restRequest);
    }

    private JsonObject verifyDetachedSignature(String uri, String id1, String id2) throws URISyntaxException,
            IOException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        headers.put("Accept", "application/json");
        String requestContent = "{\"DocumentIds\":[\"" + id1 + "\",\"" + id2 + "\"],\"CustomParams" +
                "\":{\"CertVerifiersPluginsIds\":[]}}";
        RestRequest restRequest = RestRequest.builder()
                .httpMethod(RestMethod.POST)
                .requestUri(new URI(uri + "/signatures/package/session"))
                .httpHeader(headers)
                .requestContent(requestContent)
                .build();
        return restClient.requestJson(restRequest);
    }


    private void resetAll(String uri) throws URISyntaxException, IOException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        RestRequest restRequest = RestRequest.builder()
                .httpMethod(RestMethod.POST)
                .requestUri(new URI(uri + "/documents/reset/all"))
                .requestContent("")
                .httpHeader(headers)
                .build();
        restClient.requestString(restRequest);
    }

    public static String convertToString(JsonObject jsonObject) {
        Gson gson = new Gson();
        return gson.toJson(jsonObject);
    }

    private static su.opencode.digsigcik.synapi.api.model.ContainerSelector createContainerSelector(ContainerSelectorCode containerSelectorCode) {
        val selector = new su.opencode.digsigcik.synapi.api.model.ContainerSelector();
        selector.setCode(containerSelectorCode);
        return selector;
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
}
