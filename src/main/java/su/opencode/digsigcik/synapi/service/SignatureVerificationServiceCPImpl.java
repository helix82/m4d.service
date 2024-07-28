package su.opencode.digsigcik.synapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.client.RestTemplateClient;
import su.opencode.digsigcik.synapi.dto.ReportContainerDTO;
import su.opencode.digsigcik.synapi.dto.ReportDTO;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.HashMap;

@Service
@Qualifier("cryptopro-keylab")
public class SignatureVerificationServiceCPImpl implements SignatureVerificationService {

    @Value("${cryptopro.svs.uri}")
    private String svc_uri;

    private final RestTemplateClient restTemplateClient;

    public SignatureVerificationServiceCPImpl() throws CertificateException, NoSuchAlgorithmException,
            KeyStoreException, IOException, NoSuchProviderException, KeyManagementException {
        this.restTemplateClient = new RestTemplateClient(true);
    }

    @Override
    public ReportContainerDTO verifyDetachedGetPdfReport(byte @NotNull [] sourceFile,
                                                         byte @NotNull [] detachedSignFile, String sourceFileName,
                                                         String detachedSignFileName) throws IOException,
            URISyntaxException {
        resetAll(svc_uri);
        String id1 = uploadDocument(svc_uri, sourceFile, sourceFileName).get("id")
                .getAsString();
        String id2 = uploadDocument(svc_uri, detachedSignFile, detachedSignFileName).get("id")
                .getAsString();
        String metaJsonStr = verifyDetachedSignature(svc_uri, id1, id2);
        byte[] report = getReportFile(svc_uri);
        resetAll(svc_uri);
        ReportDTO reportDTO = new ObjectMapper().readValue(metaJsonStr, ReportDTO.class);
        JSONObject json = new JSONObject(metaJsonStr);
        byte[] metaJsonContent = json.toString(4)
                .getBytes(StandardCharsets.UTF_8);
        return ReportContainerDTO.builder()
                .signCheck(metaJsonContent)
                //   .signReport(createPdf(reportDTO))
                .signReport(report)
                .build();
    }
    @Override
    public ReportContainerDTO verifyDetachedGetPdfReport(@NotNull MultipartFile sourceFile,
                                                         @NotNull MultipartFile detachedSignFile) throws IOException,
            URISyntaxException {
        return verifyDetachedGetPdfReport(sourceFile.getBytes(),
                detachedSignFile.getBytes(), sourceFile.getOriginalFilename(), detachedSignFile.getOriginalFilename());
    }
    @Override
    public ReportContainerDTO verifyAttachedGetPdfReport(byte @NotNull [] attachedSign, String attachedSignFileName) throws IOException,
            URISyntaxException {
        resetAll(svc_uri);
        String id = uploadDocument(svc_uri, attachedSign, attachedSignFileName).get("id")
                .getAsString();
        String metaJsonStr = verifyAttachedSignature(svc_uri, id);
        byte[] report = getReportFile(svc_uri);
        resetAll(svc_uri);
        ReportDTO reportDTO = new ObjectMapper().readValue(metaJsonStr, ReportDTO.class);
        JSONObject json = new JSONObject(metaJsonStr);
        byte[] metaJsonContent = json.toString(4)
                .getBytes(StandardCharsets.UTF_8);
        return ReportContainerDTO.builder()
                .signCheck(metaJsonContent)
                //   .signReport(createPdf(reportDTO))
                .signReport(report)
                .build();
    }
    @Override
    public ReportContainerDTO verifyAttachedGetPdfReport(@NotNull MultipartFile attachedSignFile) throws IOException,
            URISyntaxException {
        return verifyAttachedGetPdfReport(attachedSignFile.getBytes(), attachedSignFile.getOriginalFilename());
    }
    @Override
    public ReportDTO verifyAttached(byte[] attachedSign, String attachedSignFileName) throws Exception {
        resetAll(svc_uri);
        String id1 = uploadDocument(svc_uri, attachedSign, attachedSignFileName).get("id")
                .getAsString();
        String result = verifyAttachedSignature(svc_uri, id1);
        resetAll(svc_uri);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(result, ReportDTO.class);
    }
    @Override
    public ReportDTO verifyDetached(byte[] sourceFile, byte[] detachedSign, String sourceFileFullName,
                                    String detachedSignFileName) throws Exception {
        resetAll(svc_uri);
        String id1 = uploadDocument(svc_uri, sourceFile, sourceFileFullName).get("id")
                .getAsString();
        String id2 = uploadDocument(svc_uri, detachedSign, detachedSignFileName).get("id")
                .getAsString();
        String result = verifyDetachedSignature(svc_uri, id1, id2);
        resetAll(svc_uri);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(result, ReportDTO.class);
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

    private byte[] getReportFile(String uri) throws
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

}
