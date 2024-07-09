package su.opencode.digsigcik.synapi.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
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
import java.util.Map;

@RestController
@RequestMapping("/api/signature")
public class SignatureVerificationController {

    @Value("${cryptopro.svs.url}")
    private String svs_url;

    private RestClient restClient;

    public SignatureVerificationController() throws NoSuchAlgorithmException, KeyStoreException,
            KeyManagementException {
//        this.restClient = new RestClient("192.168.0.53", 3128);
        this.restClient = new RestClient();
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestPart("file1") MultipartFile file1, @RequestPart(
            "file2") MultipartFile file2) {
        Map<String, Object> response = new HashMap<>();
        try {
            resetAll();
            String id1 = uploadDocument(file1).get("id")
                    .getAsString();
            String id2 = uploadDocument(file2).get("id")
                    .getAsString();
            JsonArray jsonArray = getJsonArrayFromString(listUploadedDocuments());

            JsonObject result = verifySignature2(id1,id2);
            resetAll();
            response.put("success", convertToString(result));
        } catch (Exception e) {
            response.put("error", "Ошибка загрузки файла: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private JsonObject uploadDocument(MultipartFile file) throws URISyntaxException, IOException {
        RestRequest restRequest = RestRequest.builder()
                .httpMethod(RestMethod.POST)
                .requestUri(new URI(svs_url+"/documents/"))
                .bytes(file.getBytes())
                .fileName(file.getOriginalFilename())
                .build();
        return restClient.requestJson(restRequest);
    }

    private String listUploadedDocuments() throws URISyntaxException,
            IOException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        headers.put("Accept", "application/json");
        RestRequest restRequest = RestRequest.builder()
                .httpMethod(RestMethod.GET)
                .requestUri(new URI(svs_url+"/documents/"))
                .httpHeader(headers)
                .build();
        return restClient.requestString(restRequest);
    }

    private JsonObject verifySignature(String id) throws URISyntaxException,
            IOException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        headers.put("Accept", "application/json");
        String requestContent = "{\"DocumentIds\":[\"" + id + "\"],\"CustomParams\":{\"CertVerifiersPluginsIds\":[]}}";
        RestRequest restRequest = RestRequest.builder()
                .httpMethod(RestMethod.POST)
                .requestUri(new URI(svs_url+"/signatures/package/session"))
                .httpHeader(headers)
                .requestContent(requestContent)
                .build();
        return restClient.requestJson(restRequest);
    }

    private JsonObject verifySignature2(String id1,String id2) throws URISyntaxException,
            IOException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        headers.put("Accept", "application/json");
        String requestContent = "{\"DocumentIds\":[\"" + id1 + "\",\"" + id2 + "\"],\"CustomParams\":{\"CertVerifiersPluginsIds\":[]}}";
        RestRequest restRequest = RestRequest.builder()
                .httpMethod(RestMethod.POST)
                .requestUri(new URI(svs_url+"/signatures/package/session"))
                .httpHeader(headers)
                .requestContent(requestContent)
                .build();
        return restClient.requestJson(restRequest);
    }



    private void resetAll() throws URISyntaxException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, IOException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        RestRequest restRequest = RestRequest.builder()
                .httpMethod(RestMethod.POST)
                .requestUri(new URI(svs_url+"/documents/reset/all"))
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

    @PostMapping("/verify")
    public Map<String, Object> verifySignature(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String signature = request.get("signature");
        String publicKey = request.get("publicKey");

        Map<String, Object> response = new HashMap<>();

        try {
            boolean isValid = verifyCryptopro(message, signature, publicKey);
            response.put("valid", isValid);
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }

        return response;
    }

    private boolean verifyCryptopro(String message, String signature, String publicKey) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("data", message);
        requestBody.put("signature", signature);
        requestBody.put("publicKey", publicKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(svs_url, entity, Map.class);

        Map<String, Object> responseBody = responseEntity.getBody();
        boolean isValid = (boolean) responseBody.get("isValid");

        return isValid;
    }
}