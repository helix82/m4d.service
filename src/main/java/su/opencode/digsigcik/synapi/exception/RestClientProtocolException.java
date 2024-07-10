package su.opencode.digsigcik.synapi.exception;

import lombok.Data;
import org.apache.hc.client5.http.ClientProtocolException;

@Data
public class RestClientProtocolException extends ClientProtocolException {
    private int code;

    public RestClientProtocolException(String message, int code) {
        super(message);
        this.code = code;

    }
}


