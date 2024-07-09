package su.opencode.digsigcik.synapi.exception;

import lombok.Data;
import org.apache.hc.client5.http.ClientProtocolException;

@Data
public class TLSGostContextException extends ClientProtocolException {

    public TLSGostContextException(String message) {
        super(message);
    }
    public TLSGostContextException(String message, Throwable cause) {
        super(message, cause);
    }

    public TLSGostContextException(Throwable cause) {
        super(cause);
    }
}


