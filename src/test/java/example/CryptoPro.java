package example;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCSP.JCSP;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;

public class CryptoPro {


    public static void main(String[] args) throws UnsupportedEncodingException {
        Security.addProvider(new JCSP());
        Security.addProvider(new JCP());
         readKeyPairFromPkcs12("oper.pfx","1234");

        System.out.println("Decoded string: " );
    }

    public static void readKeyPairFromPkcs12(String pkcs12FilePath, String password) {
        System.setProperty("file.encoding", "UTF-8");
        Security.addProvider(new JCSP());
        try {
            // Создаем экземпляр KeyStore
            KeyStore keyStore =  KeyStore.getInstance("PFXSTORE", "JCSP");

            // Загружаем PKCS12 файл
            try (FileInputStream fis = new FileInputStream(pkcs12FilePath)) {
                keyStore.load(fis, password.toCharArray());
            }

            // Перебираем все алиасы в keystore
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();

                // Проверяем, есть ли приватный ключ для текущего алиаса
                if (keyStore.isKeyEntry(alias)) {
                    PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());
                    Certificate certificate = keyStore.getCertificate(alias);
                    PublicKey publicKey = certificate.getPublicKey();

                    System.out.println("Alias: " + alias);
                    System.out.println("Private Key: " + privateKey);
                    System.out.println("Public Key: " + publicKey);
                    System.out.println("Certificate: " + certificate);
                    System.out.println();
                }
            }
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }


}