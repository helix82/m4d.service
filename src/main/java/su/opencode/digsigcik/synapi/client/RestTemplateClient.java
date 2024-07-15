package su.opencode.digsigcik.synapi.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.CryptoPro.Crypto.CryptoProvider;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.reprov.RevCheck;
import ru.CryptoPro.ssl.Provider;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Collections;

/**
 * REST client for working with GOST TLS.
 */
@Data
@Slf4j
public class RestTemplateClient {
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String RESPONSE_MIME_TYPE = "responseMimeType";
    public static final String USER_MIME_TYPE = "userMimeType";
    public static final String UPSTREAM = "upstream";
    public static final String UPLOAD = "upload";
    public static final String UPLOAD_FILE = "upfile";
    public static final String TEXT_XML = "text/xml";
    protected static final int CONNECT_TIMEOUT = 5000;
    protected static final int READ_TIMEOUT = 5000;
    private RestTemplate restTemplate;
    private HttpHeaders sessionHeaders = new HttpHeaders();

    public RestTemplateClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, CertificateException, IOException, NoSuchProviderException {
        this(false, null, null);
    }

    public RestTemplateClient(@NonNull final String proxyHost, @NonNull final Integer proxyPort) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, CertificateException, IOException, NoSuchProviderException {
        this(false, proxyHost, proxyPort);
    }

    public RestTemplateClient(boolean isGost) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, CertificateException, IOException, NoSuchProviderException {
        this(isGost, null, null);
    }

    public RestTemplateClient(boolean isGost, String proxyHost, Integer proxyPort) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, CertificateException, IOException, NoSuchProviderException {
        if (isGost) {
            Security.addProvider(new JCP());
            Security.addProvider(new CryptoProvider());
            Security.addProvider(new RevCheck());
            Security.addProvider(new Provider());
            SSLContext sslContext = SSLContext.getInstance("GostTLS");
            String javaHome = System.getProperty("java.home");
            String cacertsPath = Paths.get(javaHome, "lib", "security", "cacerts").toString();

            KeyStore trustedKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream fis = new FileInputStream(cacertsPath);
            trustedKeyStore.load(fis, "changeit".toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("GostX509", "JTLS");
            tmf.init(trustedKeyStore);
            sslContext.init(null, tmf.getTrustManagers(), null);
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);

            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            if (proxyHost != null && proxyPort != null) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                requestFactory.setProxy(proxy);
                requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
                requestFactory.setReadTimeout(READ_TIMEOUT);
            }
            this.restTemplate = new RestTemplate(requestFactory);
        } else {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            if (proxyHost != null && proxyPort != null) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                requestFactory.setProxy(proxy);
                requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
                requestFactory.setReadTimeout(READ_TIMEOUT);
            }
            this.restTemplate = new RestTemplate(requestFactory);
        }
        configureRestTemplate();
    }

    private void configureRestTemplate() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        restTemplate.setUriTemplateHandler(factory);
        restTemplate.setInterceptors(Collections.singletonList((request, body, execution) -> {
            request.getHeaders().putAll(sessionHeaders);
            ClientHttpResponse response = execution.execute(request, body);
            if (response.getHeaders().containsKey(HttpHeaders.SET_COOKIE)) {
                sessionHeaders.add(HttpHeaders.COOKIE, String.join(";", response.getHeaders().get(HttpHeaders.SET_COOKIE)));
            }
            return response;
        }));
    }
    public byte[] downloadFile(String url) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new IOException("Failed to download file: " + response.getStatusCode());
        }
    }
    public String requestString(@NonNull final RestTemplateClientRequest restTemplateClientRequest) throws IOException {
        HttpEntity<?> requestEntity = populateRequest(restTemplateClientRequest);
        ResponseEntity<String> response = restTemplate.exchange(restTemplateClientRequest.getRequestUri(), restTemplateClientRequest.getHttpMethod(), requestEntity, String.class);
        return response.getBody();
    }

    public JsonObject requestJson(@NonNull final RestTemplateClientRequest restTemplateClientRequest) throws IOException {
        HttpEntity<?> requestEntity = populateRequest(restTemplateClientRequest);
        ResponseEntity<String> response = restTemplate.exchange(restTemplateClientRequest.getRequestUri(), restTemplateClientRequest.getHttpMethod(), requestEntity, String.class);
        return new Gson().fromJson(response.getBody(), JsonObject.class);
    }

    private HttpEntity<?> populateRequest(@NonNull RestTemplateClientRequest restTemplateClientRequest) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        if (restTemplateClientRequest.getHttpHeader() != null) {
            headers.setAll(restTemplateClientRequest.getHttpHeader());
        }

        if (restTemplateClientRequest.getRequestContent() != null) {
            return new HttpEntity<>(restTemplateClientRequest.getRequestContent(), headers);
        } else if (restTemplateClientRequest.getRequestForm() != null) {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            restTemplateClientRequest.getRequestForm().forEach(params::add);
            return new HttpEntity<>(params, headers);
        } else if (restTemplateClientRequest.getFile() != null) {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add(UPLOAD, restTemplateClientRequest.getFile());
            String mimeType = Optional.ofNullable(Files.probeContentType(restTemplateClientRequest.getFile().toPath()))
                    .orElse(APPLICATION_OCTET_STREAM);
            body.add(USER_MIME_TYPE, mimeType);
            body.add(RESPONSE_MIME_TYPE, TEXT_XML);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            return new HttpEntity<>(body, headers);
        } else if (restTemplateClientRequest.getInputStream() != null) {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add(UPSTREAM, restTemplateClientRequest.getInputStream());
            body.add(USER_MIME_TYPE, restTemplateClientRequest.getMimeType());
            body.add(RESPONSE_MIME_TYPE, TEXT_XML);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            return new HttpEntity<>(body, headers);
        } else if (restTemplateClientRequest.getBytes() != null) {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("document", new ByteArrayResource(restTemplateClientRequest.getBytes()) {
                @Override
                public String getFilename() {
                    return restTemplateClientRequest.getFileName();
                }
            });
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            return new HttpEntity<>(body, headers);
        }

        return new HttpEntity<>(headers);
    }

    @Builder
    @Data
    public static class RestTemplateClientRequest {
        @NonNull
        HttpMethod httpMethod;
        @NonNull
        URI requestUri;
        HashMap<String, String> httpHeader;
        String requestContent;
        File file;
        String fileName;
        InputStream inputStream;
        byte[] bytes;
        String mimeType;
        Map<String, String> requestForm;
    }
}
