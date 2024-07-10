package su.opencode.digsigcik.synapi.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

class JsonResponseHandler extends RestClientResponseHandler implements HttpClientResponseHandler<Optional<JsonObject>> {

    public JsonResponseHandler(RestClient restClient) {
        super(restClient);
    }
    private final Gson gson = new Gson();

    @Override
    public Optional<JsonObject> handleResponse(ClassicHttpResponse response) throws IOException, ParseException {
        handleCodeResponse(response);
        try (HttpEntity httpEntity = response.getEntity()) {
            return Optional.of(gson.fromJson(EntityUtils.toString(httpEntity, StandardCharsets.UTF_8), JsonObject.class));
        }
    }
}
