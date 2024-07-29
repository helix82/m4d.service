package su.opencode.digsigcik.synapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.lowagie.text.DocumentException;
import lombok.extern.slf4j.Slf4j;
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
import su.opencode.digsigcik.synapi.util.CertUtils;

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

import static su.opencode.digsigcik.synapi.util.ReportUtils.createPdf;

@Service
@Qualifier("cryptopro-keylab")
@Slf4j
public class SignatureVerificationServiceCPImpl implements SignatureVerificationService {

    @Value("${cryptopro.svs.uri}")
    private String svc_uri;

    private final RestTemplateClient restTemplateClient;
    private final CRLVerificationService crlVerificationService;

    public SignatureVerificationServiceCPImpl(CRLVerificationService crlVerificationService) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, NoSuchProviderException, KeyManagementException {
        this.crlVerificationService = crlVerificationService;
        this.restTemplateClient = new RestTemplateClient(true);
    }

    public ReportContainerDTO verifyDetached(@NotNull MultipartFile sourceFile,
                                             @NotNull MultipartFile detachedSign) throws IOException,
            URISyntaxException, DocumentException {
        resetAll(svc_uri);
        String id1 = uploadDocument(svc_uri, sourceFile.getBytes(), sourceFile.getOriginalFilename()).get("id")
                .getAsString();
        String id2 = uploadDocument(svc_uri, detachedSign.getBytes(), detachedSign.getOriginalFilename()).get("id")
                .getAsString();
        String metaJsonStr = verifyDetachedSignature(svc_uri, id1, id2);
        resetAll(svc_uri);

        ReportDTO reportDTO = new ObjectMapper().readValue(metaJsonStr, ReportDTO.class);

        var additionalVerificationAgainstCRLsResultsReport = crlVerificationService.verifyAgainstCRLs(
                CertUtils.getX509CertificatesFromDigitalSign( sourceFile.getBytes(), detachedSign.getBytes() )
        );

        byte[] metaJsonContent = appendAdditionalVerificationResults(metaJsonStr, additionalVerificationAgainstCRLsResultsReport)
                .toString(4)
                .getBytes(StandardCharsets.UTF_8);

        return ReportContainerDTO.builder()
                .signCheck(metaJsonContent)
                .signReport(createPdf(reportDTO))
                .build();
    }

    public ReportContainerDTO verifyAttached(@NotNull MultipartFile attachedSign) throws IOException,
            URISyntaxException, DocumentException {
        resetAll(svc_uri);
        String id = uploadDocument(svc_uri, attachedSign.getBytes(), attachedSign.getOriginalFilename()).get("id")
                .getAsString();
        String metaJsonStr = verifyAttachedSignature(svc_uri, id);
        resetAll(svc_uri);
        ReportDTO reportDTO = new ObjectMapper().readValue(metaJsonStr, ReportDTO.class);

        var additionalVerificationAgainstCRLsResultsReport = crlVerificationService.verifyAgainstCRLs(
                CertUtils.getX509CertificatesFromDigitalSign(attachedSign.getBytes())
        );

        byte[] metaJsonContent = appendAdditionalVerificationResults(metaJsonStr, additionalVerificationAgainstCRLsResultsReport)
                .toString(4)
                .getBytes(StandardCharsets.UTF_8);

        return ReportContainerDTO.builder()
                .signCheck(metaJsonContent)
                .signReport(createPdf(reportDTO))
                .build();
    }

    private JSONObject appendAdditionalVerificationResults(String json, ReportDTO.Result.VerificationResult verificationResult) throws JsonProcessingException {

        JSONObject root = new JSONObject(json);
        JSONObject verificationResultNode = new JSONObject(new ObjectMapper().writeValueAsString(verificationResult));

        // FIXME: hardcoded af
        root.getJSONArray("Results").getJSONObject(0)
                .getJSONArray("VerificationResults")
                .put(verificationResultNode);

//                    ReportDTO r = new ObjectMapper().readValue(json, ReportDTO.class)
//            r.getResults().get(0).getVerificationResults().add(verificationResult);
//            return new JSONObject(new ObjectMapper().writeValueAsString(r));

        return root;
    }


    public ReportDTO verifyAttached(byte[] srcFile, String sourceFileFullName) throws Exception {
        resetAll(svc_uri);
        String id1 = uploadDocument(svc_uri, srcFile, sourceFileFullName + ".sig").get("id")
                .getAsString();
        String result = verifyAttachedSignature(svc_uri, id1);
        resetAll(svc_uri);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(result, ReportDTO.class);
    }

    public ReportDTO verifyDetached(byte[] srcFile, byte[] detachedSign, String sourceFileFullName) throws Exception {
        resetAll(svc_uri);
        String id1 = uploadDocument(svc_uri, srcFile, sourceFileFullName).get("id")
                .getAsString();
        String id2 = uploadDocument(svc_uri, detachedSign, sourceFileFullName + ".sig").get("id")
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
