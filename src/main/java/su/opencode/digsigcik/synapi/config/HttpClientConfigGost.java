package su.opencode.digsigcik.synapi.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

@Slf4j
public class HttpClientConfigGost extends HttpClientConfig {
    @Override
    public PoolingHttpClientConnectionManager poolingConnectionManager() throws NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException {
        SSLContext sslCtx = SSLContext.getInstance("GostTLS");
        KeyStore trustedKeyStore = KeyStore.getInstance("JKS");
        TrustManagerFactory tmf = null;
        try {
            trustedKeyStore.load(new FileInputStream("store.jks"), "changeit".toCharArray());

            tmf = TrustManagerFactory.getInstance("GostX509", "JTLS");
        } catch (NoSuchProviderException | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
        tmf.init(trustedKeyStore);
        sslCtx.init(null, tmf.getTrustManagers(), null);
        SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder
                .create()
                .setSslContext(sslCtx)
                .setHostnameVerifier(new NoopHostnameVerifier())
                .build();
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();
        PoolingHttpClientConnectionManager poolingConnectionManager =
                new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        poolingConnectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        poolingConnectionManager.setDefaultMaxPerRoute(MAX_TOTAL_CONNECTIONS);
        poolingConnectionManager.setDefaultConnectionConfig(connectionConfig);
        return poolingConnectionManager;
    }

}
