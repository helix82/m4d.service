package su.opencode.digsigcik.synapi.client;

import org.apache.hc.client5.http.auth.AuthenticationException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

class StringResponseHandler extends RestClientResponseHandler implements HttpClientResponseHandler<Optional<String>> {

    public StringResponseHandler(RestClient restClient) {
        super(restClient);
    }

    @Override
    public Optional<String> handleResponse(ClassicHttpResponse response) throws IOException, ParseException {
            handleCodeResponse(response);
            try (HttpEntity httpEntity = response.getEntity()) {
                return Optional.of(EntityUtils.toString(httpEntity, StandardCharsets.UTF_8));
            }
    }
}
