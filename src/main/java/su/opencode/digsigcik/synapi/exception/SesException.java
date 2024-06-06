package su.opencode.digsigcik.synapi.exception;

public class SesException extends RuntimeException {
    public SesException(String message) {
        super(message);
    }
    public SesException(String message, Throwable cause) {
        super(message, cause);
    }
}
