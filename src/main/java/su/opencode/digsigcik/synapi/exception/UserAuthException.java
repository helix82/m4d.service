package su.opencode.digsigcik.synapi.exception;
//TODO @vitalik.sahnov Временное решение. Удалить после интеграции авторизации
public class UserAuthException extends RuntimeException {
    public UserAuthException(String message) {
        super(message);
    }
    public UserAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
