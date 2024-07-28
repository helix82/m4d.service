package example.ca;


import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCPRequest.ca20.request.CA20GostCertificateRequest;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.reprov.RevCheck;
import ru.CryptoPro.ssl.Provider;

import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/**
 * ример получения списка корневых сертификатов УЦ (CA20).
 *
 * @author Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class GetCA20RootCertificateExample extends CAConfiguration {


    /**
     * Конструктор.
     *
     * @param needInit Флаг необходимости выполнить
     * инициализацию.
     */
    protected GetCA20RootCertificateExample(boolean needInit) {
        super(needInit);
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        Security.addProvider(new JCP()); // провайдер JCP
        //  Security.addProvider(new JS()); // провайдер JCP
        Security.addProvider(new RevCheck());
        Security.addProvider(new Provider());
        JCPInit.initProviders(true);
        final GetCA20RootCertificateExample example = new GetCA20RootCertificateExample(true);
        example.main();

    }

    @Override
    public void main() throws Exception {

        Certificate[] rootCerts = CA20GostCertificateRequest
                .getRootCertList(CA20_ADDRESS);

        if (rootCerts != null) {

            // Выводим сертификаты на экран.
            for (int i = 0; i < rootCerts.length; i++) {

                X509Certificate cert = (X509Certificate) rootCerts[i];
                System.out.println("Root certificates (CA20): " +
                        "\n\tserial number: " + cert.getSerialNumber().toString(16) +
                        "\n\tsubject: " + cert.getSubjectDN() +
                        "\n\tissuer: " + cert.getIssuerDN());
            } // for

        } // if

    }

}
