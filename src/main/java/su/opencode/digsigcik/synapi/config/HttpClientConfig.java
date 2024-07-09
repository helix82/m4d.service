package su.opencode.digsigcik.synapi.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * - Supports both HTTP and HTTPS
 * - Uses a connection pool to re-use connections and save overhead of creating connections.
 * - Has a custom connection keep-alive strategy (to apply a default keep-alive if one isn't specified)
 * - Starts an idle connection monitor to continuously clean up stale connections.
 */
@Slf4j
public class HttpClientConfig {
    // Determines the timeout in SECONDS until a connection is established.
    public static final int CONNECT_TIMEOUT = 600;
    // The timeout in SECONDS when requesting a connection from the connection manager.
    public static final int REQUEST_TIMEOUT = 600;
    // The timeout in MINUTES for waiting for data
    public static final int RESPONSE_TIMEOUT = 20;
    public static final int MAX_TOTAL_CONNECTIONS = 5000;
    public static final int DEFAULT_KEEP_ALIVE_TIME_MILLIS = 20 * 1000;
    public static final int CLOSE_IDLE_CONNECTION_WAIT_TIME_SECS = 5;

    public static final int VALIDATE_AFTER_INACTIVITY = 1000;

    public static final boolean RETRY_FAILED_REQUESTS_ON_CUSTOM_EXCEPTION_OR_CODES = false;
    public static final int MAX_RETRIES = 10;
    public static final long RETRY_INTERVAL_IN_SECONDS = 60;
    Collection<Class<? extends IOException>> NOT_RETRY_ON_EXCEPTION_CLASSES = Arrays.asList ();
    Collection<Integer> RETRY_ON_CODES= Arrays.asList ();
    
    private final RequestConfig requestConfig = RequestConfig.custom()
            .setRedirectsEnabled(true)
            .setDefaultKeepAlive(DEFAULT_KEEP_ALIVE_TIME_MILLIS, TimeUnit.MILLISECONDS)
            .setCookieSpec(StandardCookieSpec.RELAXED)
            .setResponseTimeout(RESPONSE_TIMEOUT, TimeUnit.MINUTES)
            .setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .setConnectionRequestTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .build();

    private final ConnectionConfig connectionConfig = ConnectionConfig.custom()
            .setValidateAfterInactivity(VALIDATE_AFTER_INACTIVITY, TimeUnit.MILLISECONDS)
            .setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .setSocketTimeout(RESPONSE_TIMEOUT, TimeUnit.MINUTES)
            .build();

    public PoolingHttpClientConnectionManager poolingConnectionManager() throws NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException {
        SSLContextBuilder builder = SSLContexts.custom();
        builder.loadTrustMaterial(null, (chain, authType) -> true);
        SSLContext sslContext = null;
        try {
            sslContext =new TLSGostContext().getContext();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslsf)
                .build();

        PoolingHttpClientConnectionManager poolingConnectionManager =
                new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        poolingConnectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        poolingConnectionManager.setDefaultMaxPerRoute(MAX_TOTAL_CONNECTIONS);
        poolingConnectionManager.setDefaultConnectionConfig(connectionConfig);
        return poolingConnectionManager;
    }

    private final HttpRequestRetryStrategy retryStrategy = resolveHttpRequestRetryStrategy();


    public CloseableHttpClient httpClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(poolingConnectionManager())
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(requestConfig.getCookieSpec())
                        .build())
                .setRetryStrategy(retryStrategy)
                .build();
    }

    public CloseableHttpClient httpClient(HttpHost proxy) throws NoSuchAlgorithmException, KeyStoreException,
            KeyManagementException {

        return HttpClients.custom()
                .setProxy(proxy)
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(poolingConnectionManager())
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(requestConfig.getCookieSpec())
                        .build())
                .setRetryStrategy(retryStrategy)
                .build();
    }

    private HttpRequestRetryStrategy resolveHttpRequestRetryStrategy() {
        if (RETRY_FAILED_REQUESTS_ON_CUSTOM_EXCEPTION_OR_CODES) {
            return  new CustomHttpRequestRetryStrategy(MAX_RETRIES,
                    TimeValue.ofSeconds(RETRY_INTERVAL_IN_SECONDS), NOT_RETRY_ON_EXCEPTION_CLASSES, RETRY_ON_CODES);
        }
        else {
            return new CustomHttpRequestRetryStrategy(0,TimeValue.ofSeconds(0L), Collections.emptyList(),Collections.emptyList());
        }
    }

}
