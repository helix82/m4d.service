package su.opencode.digsigcik.m4d.cert;

import java.util.Objects;

public class CertificateParsingException extends RuntimeException {

    /**
     * Такого исключения не должно появляться в ходе работы приложения.
     */
    public static final String MESSAGE = "Что-то пошло не так при попытке распарсить сертификат";



    private String customMessage = null;

    public CertificateParsingException(String message) {
        super(message);
    }

    public CertificateParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CertificateParsingException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return Objects.requireNonNullElse(customMessage, super.getMessage());
    }

    public void replaceMessage(String message) {
        this.customMessage = message;
    }
}
