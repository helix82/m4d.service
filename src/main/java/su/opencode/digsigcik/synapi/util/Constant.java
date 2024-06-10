package su.opencode.digsigcik.synapi.util;

public final class Constant {
    public static final String NULL = "null";
    public static final String NULL_OR_EMPTY_ERROR_MESSAGE = String.format("%s or empty!", NULL);
    public static final String TOKEN_ERROR_MESSAGE = String.format("Token %s", NULL_OR_EMPTY_ERROR_MESSAGE);
    public static final String LOGIN_ERROR_MESSAGE = String.format("Login %s", NULL_OR_EMPTY_ERROR_MESSAGE);
    public static final String PASSWORD_ERROR_MESSAGE = String.format("Password %s", NULL_OR_EMPTY_ERROR_MESSAGE);
    public static final String PIN_CODE_ERROR_MESSAGE = "PIN code incorrect!";
    public static final String OPERATION_ID_ERROR_MESSAGE = String.format("Operation id %s", NULL_OR_EMPTY_ERROR_MESSAGE);
    public static final String FILE_ERROR_MESSAGE = String.format("File %s", NULL_OR_EMPTY_ERROR_MESSAGE);
    public static final String NOT_FOUND = "not found!";
    public static final String ID_IS_NULL = String.format("ID is %s", NULL);
    public static final String USER_NOT_FOUND = String.format("User %s", NOT_FOUND);
    public static final String EMPTY_BYTE_ARRAY_ERROR = "Empty byte array!";
    public static final String AUTH_ERROR = "Authorisation Error!";
}
