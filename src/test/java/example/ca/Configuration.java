package example.ca;

public interface Configuration {

    /**
     * Адрес УЦ (CA15).
     */
    public static final String CA15_ADDRESS_HTTP = "http://www.cryptopro.ru/ui";

    /**
     * Адрес УЦ (CA15).
     */
    public static final String CA15_ADDRESS_HTTPS = "https://www.cryptopro.ru/ui";

    /**
     * Адрес УЦ (CA20).
     */
    public static final String CA20_ADDRESS = "https://keylab:444";

    /**
     * Поле CN (CommonName) (CA15).
     */
    // TODO заменить имя.
    public static final String COMMON_NAME = "afevma_555";
    /**
     * Поле C (CountryName) (CA15).
     */
    public static final String COUNTRY_NAME = "RU";
    /**
     * Маркер зарегистрированного пользователя (CA15).
     */
    // TODO заменить маркер.
    public static final String TOKEN_ID = "18786";
    /**
     * Пароль зарегистрированного пользователя (CA15).
     */
    // TODO заменить пароль.
    public static final String PASSWORD = "xq5rvr22";

    /**
     * Папка пользователя УЦ (CA20).
     */
    public static final String CS20_USER_FOLDER = "b1ca4992-d7cd-4f7e-b56e-a81e00db58ee";

}
