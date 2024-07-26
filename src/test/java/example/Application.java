package example;

import lombok.extern.slf4j.Slf4j;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

@Slf4j
public class Application {
    public static void main(String[] args) throws UnsupportedEncodingException {
        KeyPair keyPair = readKeyPairFromPkcs12("oper.pfx","1234");

        System.out.println("Decoded string: " );
    }

    public static KeyPair readKeyPairFromPkcs12(String filePath, String password) {
        BouncyCastleProvider bcProvider = new BouncyCastleProvider();
        String name = bcProvider.getName();
        Security.removeProvider(name); // remove old instance
        Security.addProvider(bcProvider);
        try (FileInputStream fis = new FileInputStream(filePath)) {
            // Загружаем PKCS#12 хранилище
            KeyStore keyStore = null;
            try {
                keyStore = KeyStore.getInstance("PKCS12", "BC");
            } catch (NoSuchProviderException e) {
                throw new RuntimeException(e);
            }
            keyStore.load(fis, password.toCharArray());

            // Находим первый доступный сертификат и извлекаем соответствующие ключи
            String alias = keyStore.aliases()
                    .nextElement();
            Key key = keyStore.getKey(alias, password.toCharArray());
            if (key instanceof PrivateKey) {
                // Get certificate of public key
                Certificate cert = keyStore.getCertificate(alias);

                // Get public key
                PublicKey publicKey = cert.getPublicKey();

                // Return a key pair
                return new KeyPair(publicKey, (PrivateKey) key);
            }
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException |
                 UnrecoverableEntryException e) {
            throw new RuntimeException("Ошибка при чтении ключей из PKCS#12 файла", e);
        }
        return null;
    }
}