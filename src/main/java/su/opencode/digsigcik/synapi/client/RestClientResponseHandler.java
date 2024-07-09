package su.opencode.digsigcik.synapi.client;


import lombok.Data;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import su.opencode.digsigcik.synapi.exception.RestClientProtocolException;

import java.io.IOException;
@Data
class RestClientResponseHandler {
    private final RestClient restClient;

    public RestClientResponseHandler(RestClient restClient) {
        this.restClient = restClient;
    }

    public void handleCodeResponse(ClassicHttpResponse response) throws IOException, ParseException {
        final int status = response.getCode();
        if (!(status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION)) {
            HttpEntity entity = response.getEntity();
            String stringEntity;
            if (entity == null) {
                stringEntity = "null response entity";
            } else {
                stringEntity = EntityUtils.toString(entity);
            }
            throw new RestClientProtocolException(String.format("Unexpected response status code: %s : %s \n %s",
                    response.getCode(), response.getReasonPhrase(),
                    stringEntity),response.getCode());
        }
    }
}
