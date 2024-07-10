package su.opencode.digsigcik.synapi.config;

import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.utils.DateUtils;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.CancellableDependency;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.*;

@Contract(
        threading = ThreadingBehavior.STATELESS
)
public class CustomHttpRequestRetryStrategy implements HttpRequestRetryStrategy {
    public static final CustomHttpRequestRetryStrategy INSTANCE = new CustomHttpRequestRetryStrategy();
    private final int maxRetries;
    private final TimeValue defaultRetryInterval;
    private final Set<Class<? extends IOException>> nonRetriableIOExceptionClasses;
    private final Set<Integer> retriableCodes;

    public CustomHttpRequestRetryStrategy(int maxRetries, TimeValue defaultRetryInterval, Collection<Class<? extends IOException>> clazzes, Collection<Integer> codes) {
        Args.notNegative(maxRetries, "maxRetries");
        Args.notNegative(defaultRetryInterval.getDuration(), "defaultRetryInterval");
        this.maxRetries = maxRetries;
        this.defaultRetryInterval = defaultRetryInterval;
        this.nonRetriableIOExceptionClasses = new HashSet<>(clazzes);
        this.retriableCodes = new HashSet<>(codes);
    }

    public CustomHttpRequestRetryStrategy(int maxRetries, TimeValue defaultRetryInterval) {
        this(maxRetries, defaultRetryInterval, Arrays.asList(InterruptedIOException.class, UnknownHostException.class, ConnectException.class, ConnectionClosedException.class, NoRouteToHostException.class, SSLException.class), Arrays.asList(403, 429, 503));
    }

    public CustomHttpRequestRetryStrategy() {
        this(1, TimeValue.ofSeconds(1L));
    }

    public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
         if (execCount > this.maxRetries) {
            return false;
        } else if (this.nonRetriableIOExceptionClasses.contains(exception.getClass())) {
            return false;
        } else {
            Iterator<Class<? extends IOException>> var5 = this.nonRetriableIOExceptionClasses.iterator();

            Class<? extends IOException> rejectException;
            do {
                if (!var5.hasNext()) {
                    if (request instanceof CancellableDependency && ((CancellableDependency)request).isCancelled()) {
                        return false;
                    }

                    return this.handleAsIdempotent(request);
                }

                rejectException = var5.next();
            } while(!rejectException.isInstance(exception));

            return false;
        }
    }

    public boolean retryRequest(HttpResponse response, int execCount, HttpContext context) {
        return execCount <= this.maxRetries && this.retriableCodes.contains(response.getCode());
    }

    public TimeValue getRetryInterval(HttpResponse response, int execCount, HttpContext context) {
        Header header = response.getFirstHeader("Retry-After");
        TimeValue retryAfter = null;
        if (header != null) {
            String value = header.getValue();

            try {
                retryAfter = TimeValue.ofSeconds(Long.parseLong(value));
            } catch (NumberFormatException var9) {
                Instant retryAfterDate = DateUtils.parseStandardDate(value);
                if (retryAfterDate != null) {
                    retryAfter = TimeValue.ofMilliseconds(retryAfterDate.toEpochMilli() - System.currentTimeMillis());
                }
            }

            if (TimeValue.isPositive(retryAfter)) {
                return retryAfter;
            }
        }

        return this.defaultRetryInterval;
    }

    protected boolean handleAsIdempotent(HttpRequest request) {
        return Method.isIdempotent(request.getMethod());
    }
}
