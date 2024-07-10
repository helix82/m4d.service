package su.opencode.digsigcik.synapi.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.client.RestClient;
import su.opencode.digsigcik.synapi.client.RestMethod;
import su.opencode.digsigcik.synapi.client.RestRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

@RestController
@RequestMapping("/api/verify")
@Slf4j
public class VerificationController {

    @Value("${cryptopro.svs.internal_url}")
    private String svc_internal_url;
    @Value("${cryptopro.svs.external_url}")
    private String svs_external_url;

    private RestClient restClient = null;

    @PostMapping(value = "/internal/detached",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> verifyDetachedInternal(@RequestPart("file1") MultipartFile file1, @RequestPart(
            "file2") MultipartFile file2) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        this.restClient = new RestClient(true);
        return verifyDetachedSignature(svc_internal_url,file1, file2);
    }

    @PostMapping(value = "/internal/attached",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> verifyAttachedInternal(@RequestPart("file1") MultipartFile file) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        this.restClient = new RestClient(true);
        return verifyAttachedSignature(svc_internal_url,file);
    }

    @PostMapping(value = "/external/detached",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> verifyDetachedExternal(@RequestPart("file1") MultipartFile file1, @RequestPart(
            "file2") MultipartFile file2) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        this.restClient = new RestClient(false);
        return verifyDetachedSignature(svs_external_url,file1, file2);
    }

    @PostMapping(value = "/external/attached",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> verifyAttachedExternal(@RequestPart("file1") MultipartFile file) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        this.restClient = new RestClient(false);
        return verifyAttachedSignature(svs_external_url,file);
    }


    public ResponseEntity<String> verifyDetachedSignature(String uri, MultipartFile file1, MultipartFile file2) {
        String res = "";
        try {
            resetAll(uri);
            String id1 = uploadDocument(uri,file1).get("id")
                    .getAsString();
            String id2 = uploadDocument(uri, file2).get("id")
                    .getAsString();
            JsonObject result = verifyDetachedSignature(uri,id1, id2);
            resetAll(uri);
            res = convertToString(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(res);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    public ResponseEntity<String> verifyAttachedSignature(String uri, MultipartFile file) {
        String res = "";
        try {
            resetAll(uri);
            String id1 = uploadDocument(uri,file).get("id")
                    .getAsString();
            JsonObject result = verifyAttachedSignature(uri,id1);
            resetAll(uri);
            res = convertToString(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(res);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
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

    private JsonObject verifyAttachedSignature(String uri,String id) throws URISyntaxException,
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

    public static JsonArray getJsonArrayFromString(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonArray jsonArray = parser.parse(jsonString)
                .getAsJsonArray();
        return jsonArray;
    }

    public static String convertToString(JsonObject jsonObject) {
        Gson gson = new Gson();
        return gson.toJson(jsonObject);
    }

}