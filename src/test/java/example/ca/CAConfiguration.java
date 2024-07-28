package example.ca;

import ru.CryptoPro.JCP.JCP;

/**
 * Класс используется для инициализации параметров
 * подключения в примерах.
 *
 * @author Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public abstract class CAConfiguration implements Configuration {

    /**
     * Текущая папка.
     */
    private static final String CURRENT_DIR = System.getProperty("user.dir");

    /**
     * Хранилище доверенных корневых сертификатов.
     */
    public static final String TRUST_STORE = CURRENT_DIR +
            "/data/KEYS/local_ca/CA15TrustedStore.store";


    /**
     * Хранилище доверенных корневых сертификатов.
     */
    public static final String CERT_STORE = CURRENT_DIR +
            "/data/CERTS/testca2012_ca/";

    /**
     * Пароль для доступа к хранилищу доверенных
     * корневых сертификатов.
     */
    public static final String TRUST_STORE_PASSWORD = "1";

    /**
     * Флаг инициализации.
     */
    private static boolean initiated = false;

    /**
     * Конструктор.
     *
     * @param needInit Флаг необходимости выполнить
     * инициализацию.
     */
    protected CAConfiguration(boolean needInit) {

        if (!initiated) {

            if (needInit) {
                init();
            } // if

        } // if

        initiated = true;

    }

    /**
     * Функция запуска примера.
     *
     * @throws Exception
     */
    public abstract void main() throws Exception;

    /**
     * Функция инициализации.
     *
     */
    private static void init() {

        // Включаем возможность онлайновой проверки.
        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");

        // Настройки TLS, т.к. обращение к УЦ 2.0 будет выполняться
        // по протоколу HTTPS. Указываем тип хранилища с доверенным
        // корневым сертификатом, путь к нему и пароль.
        System.setProperty("javax.net.ssl.trustStoreType", JCP.CERT_STORE_NAME);
        System.setProperty("javax.net.ssl.trustStore", TRUST_STORE);
        System.setProperty("javax.net.ssl.trustStorePassword", TRUST_STORE_PASSWORD);

    }

}
