package su.opencode.digsigcik.synapi.util;

public final class Constant {
    public static final String NULL = "null";
    public static final String NULL_OR_EMPTY_ERROR_MESSAGE = String.format("%s or empty!", NULL);
    public static final String TOKEN_ERROR_MESSAGE = String.format("Token %s", NULL_OR_EMPTY_ERROR_MESSAGE);
    public static final String PIN_CODE_ERROR_MESSAGE = "PIN code incorrect!";
    public static final String FILE_ERROR_MESSAGE = String.format("File %s", NULL_OR_EMPTY_ERROR_MESSAGE);

    public static final String NOT_FOUND = "not found!";
    public static final String ID_IS_NULL = String.format("ID is %s", NULL);
    public static final String JWT_USER_NOT_FOUND = String.format("JWT User %s", NOT_FOUND);
    public static final String USER_PROJECTION_NOT_FOUND = String.format("User projection %s", NOT_FOUND);
    public static final String PIN_CODE_NOT_FOUND = String.format("Pin code %s", NOT_FOUND);
    public static final String EMPTY_BYTE_ARRAY_ERROR = "Empty byte array!";
    public static final String AUTH_ERROR = "Authorisation Error!";
    public static final String ROOT_FILE_STORAGE_PATH = ".";
    public static final String DSCP_DIRECTORY_PATH = "/files/dscp/";
    public static final String SOURCE_DIRECTORY_PATH = "/files/source/";
    public static final String HASH_ALGORITHM = "SHA-256";
}
