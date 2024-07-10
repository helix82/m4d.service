package su.opencode.digsigcik.synapi.client;


import com.google.gson.JsonObject;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import su.opencode.digsigcik.synapi.config.HttpClientConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * REST client for working with GOST TLS.
 */
@Data
@Slf4j
public class RestClient {
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    //WrappedResource
    public static final String RESPONSE_MIME_TYPE = "responseMimeType";
    public static final String USER_MIME_TYPE = "userMimeType";
    public static final String UPSTREAM = "upstream";
    public static final String UPLOAD = "upload";
    public static final String UPLOAD_FILE = "upfile";
    public static final String TEXT_XML = "text/xml";
    private static final String[] GENERIC_TLDS_PLUS = new String[]{"rdm"};

    static {
        try {
            DomainValidator.updateTLDOverride(DomainValidator.ArrayType.GENERIC_PLUS, GENERIC_TLDS_PLUS);
        } catch (IllegalStateException ise) {
            log.warn("DomainValidator is already initiated");
        }
    }

    private CloseableHttpClient httpClient;


    /**
     * Constructors.
     *
     */
    public RestClient(
    ) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        super();
        this.httpClient = new HttpClientConfig().httpClient();
    }

    /**
     * Constructors.
     *
     * @param proxyHost proxyHost
     * @param proxyPort proxyPort
     */
    public RestClient(
            @NonNull final String proxyHost,
            @NonNull final Integer proxyPort
    ) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        super();
        HttpHost proxy = new HttpHost(proxyHost, proxyPort);
        this.httpClient = new HttpClientConfig().httpClient(proxy);
    }

    public String requestString(@NonNull final RestRequest restRequest) throws IOException {
        ClassicHttpRequest request = populateRequest(restRequest);
        return requestString(request);
    }

    public JsonObject requestJson(@NonNull final RestRequest restRequest) throws IOException {
        ClassicHttpRequest request = populateRequest(restRequest);
        return requestJson(request);
    }


    private ClassicHttpRequest populateRequest(@NonNull RestRequest restRequest) throws IOException {
        ClassicHttpRequest request;
        StringEntity stringEntity;
        switch (restRequest.httpMethod) {
            case GET:
                request = new HttpGet(restRequest.requestUri);
                break;
            case POST:
                request = new HttpPost(restRequest.requestUri);
                if (restRequest.requestContent != null) {
                    stringEntity = new StringEntity(restRequest.requestContent, StandardCharsets.UTF_8);
                    request.setEntity(stringEntity);
                } else if (restRequest.requestForm != null) {
                    List<NameValuePair> params = restRequest.requestForm.entrySet()
                            .stream()
                            .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList());
                    request.setEntity(new UrlEncodedFormEntity(params));
                } else if (restRequest.file != null) {
                    FileBody bin = new FileBody(restRequest.file);
                    Path path = restRequest.file.toPath();
                    String mimeType = null;
                    try {
                        mimeType = Files.probeContentType(path);
                    } catch (IOException e) {
                        log.error("error encode mimeType for {}", restRequest.file.getAbsolutePath(), e);
                    }
                    if (mimeType == null) {
                        mimeType = APPLICATION_OCTET_STREAM;
                    }
                    HttpEntity httpEntity = MultipartEntityBuilder.create()
                            .setMode(HttpMultipartMode.EXTENDED)
                            .addPart(UPLOAD, bin)
                            .addTextBody(USER_MIME_TYPE, mimeType, ContentType.DEFAULT_TEXT)
                            .addTextBody(RESPONSE_MIME_TYPE, TEXT_XML, ContentType.DEFAULT_TEXT)
                            .setContentType(ContentType.MULTIPART_FORM_DATA.withCharset(""))
                            .build();
                    request.setEntity(httpEntity);
                } else if (restRequest.inputStream != null) {
                    HttpEntity httpEntity = MultipartEntityBuilder.create()
                            .setMode(HttpMultipartMode.EXTENDED)
                            .addBinaryBody(UPSTREAM, restRequest.inputStream,
                                    ContentType.create(restRequest.mimeType), restRequest.fileName)
                            .addTextBody(USER_MIME_TYPE, restRequest.mimeType)
                            .addTextBody(RESPONSE_MIME_TYPE, TEXT_XML)
                            .setContentType(ContentType.MULTIPART_FORM_DATA.withCharset(""))
                            .build();
                    request.setEntity(httpEntity);
                } else if (restRequest.bytes != null) {
                    HttpEntity httpEntity = MultipartEntityBuilder.create()
                            .setMode(HttpMultipartMode.EXTENDED)
                            .addBinaryBody("document", restRequest.bytes, ContentType.DEFAULT_BINARY,
                                    restRequest.fileName)
//                            .addTextBody(USER_MIME_TYPE, restRequest.mimeType)
//                            .addTextBody(RESPONSE_MIME_TYPE, TEXT_XML)
                            .setContentType(ContentType.MULTIPART_FORM_DATA.withCharset(""))
                            .build();
                    request.setEntity(httpEntity);
                }

                break;
            case PUT:
                request = new HttpPut(restRequest.requestUri);
                stringEntity = new StringEntity(restRequest.requestContent, StandardCharsets.UTF_8);
                request.setEntity(stringEntity);
                break;
            case DELETE:
                request = new HttpDelete(restRequest.requestUri);
                break;
            case HEAD:
                request = new HttpHead(restRequest.requestUri);
                break;
            default:
                throw new IOException("Invalid Method");
        }
        if (restRequest.httpHeader != null) {
            populateHeaders(request, restRequest.httpHeader);
        }
        return request;
    }

    private String requestString(@NonNull final ClassicHttpRequest request) throws IOException {
        Optional<String> result = httpClient.execute(request, new StringResponseHandler(this));
        return result.isPresent() ? result.get() : requestString(request);
    }

    private JsonObject requestJson(@NonNull final ClassicHttpRequest request) throws IOException {
        Optional<JsonObject> result = httpClient.execute(request, new JsonResponseHandler(this));
        return result.isPresent() ? result.get() : requestJson(request);
    }

    /**
     * populate Custom Headers
     *
     * @param request request
     * @param headers headers
     */
    private void populateHeaders(@NonNull ClassicHttpRequest request, @NonNull final Map<String, String> headers) {
        for (Map.Entry<String, String> mapElement : headers.entrySet()) {
            if (request.getFirstHeader(mapElement.getKey()) != null) {
                if (mapElement.getValue() != null) {
                    request.setHeader(mapElement.getKey(), mapElement.getValue());
                } else {
                    request.removeHeaders(mapElement.getKey());
                }
            } else {
                if (mapElement.getValue() != null) {
                    request.addHeader(mapElement.getKey(), mapElement.getValue());
                }
            }
        }
    }
}