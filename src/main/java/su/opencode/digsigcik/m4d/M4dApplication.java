package su.opencode.digsigcik.m4d;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import su.opencode.digsigcik.specification.annotation.EnableSpecificationHandling;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@SpringBootApplication
@EnableSpecificationHandling
public class M4dApplication {
    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.trustStore", System.getenv("JAVA_HOME")+"/lib/security/cacerts");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        SpringApplication.run(M4dApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() throws NoSuchAlgorithmException, KeyManagementException {
        // Force TLS 1.2
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, null, null);
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                sslContext,
                new String[]{"TLSv1.2"},  // Supported protocols
                null,
                NoopHostnameVerifier.INSTANCE // Change if you need strict verification
        );

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(socketFactory)
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        return restTemplate;
    }
}
