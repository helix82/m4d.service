package example;

import lombok.extern.log4j.Log4j2;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import ru.CryptoPro.CAdES.tools.verifier.GostContentSignerProvider;
import ru.CryptoPro.CAdES.tools.verifier.GostDigestCalculatorProvider;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.reprov.RevCheck;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Log4j2
public class TestSMSSign {
    public static void main(String[] args) throws Exception {
        //   Security.addProvider(new JCP());
        Security.addProvider(new JCSP());
        Security.addProvider(new RevCheck());
        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");
//        System.setProperty("com.sun.security.enableAIAcaIssuers", "true");
//        System.setProperty("ru.CryptoPro.reprov.enableAIAcaIssuers", "true");
        byte[] data = "qwerty".getBytes(StandardCharsets.UTF_8);
        byte[] s = createSignature(data);
        saveByteArrayToFile(s, "test.p7s");
        saveByteArrayToFile(data, "test.txt");
        System.out.println("Done");
    }

    public static byte[] createSignature(byte[] data)
            throws Exception {
        String pass = "301214";
        KeyPair keyPair = null;
        X509Certificate certificate = null;
        List<X509Certificate> certificates = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream("oper1.pfx")) {
            // Загружаем PKCS#12 хранилище
            KeyStore keyStore = null;
            try {
                keyStore = KeyStore.getInstance("PFXSTORE", "JCSP");
            } catch (NoSuchProviderException e) {
                throw new RuntimeException(e);
            }
            keyStore.load(fis, pass.toCharArray());
            Enumeration<String> aliases = keyStore.aliases();

            // Перебираем все алиасы в keystore

            PrivateKey privateKey = null;
            PublicKey publicKey = null;
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                // Проверяем, есть ли приватный ключ для текущего алиаса
                if (keyStore.isKeyEntry(alias)) {
                    privateKey = (PrivateKey) keyStore.getKey(alias, pass.toCharArray());
                    if (privateKey != null) {
                        Certificate[] certificateChain = keyStore.getCertificateChain(alias);

                        for (int i = 0, length = certificateChain == null ? 0 : certificateChain.length; i < length; i++) {
                            certificates.add((X509Certificate) certificateChain[i]);
                        }
                        certificate = (X509Certificate) keyStore.getCertificate(alias);
                        publicKey = certificate.getPublicKey();
                        log.debug("Alias: " + alias);
                        log.debug("Private Key: " + privateKey);
                        log.debug("Public Key: " + publicKey);
                        log.debug("Certificate: " + certificate);
                    }
                }
            }
            keyPair = new KeyPair(publicKey, privateKey);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException |
                 UnrecoverableEntryException e) {
            throw new RuntimeException("Ошибка при чтении ключей из PKCS#12 файла", e);
        }

        ru.CryptoPro.CAdES.tools.Utility.initJCPAlgorithms();

        // Сертификат подписи - первый в списке.
        X509Certificate signerCert = certificates.iterator().next();
        JcaCertStore certs = new JcaCertStore(certificates);
        // Подготавливаем подпись.
       CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                new GostDigestCalculatorProvider(keyPair.getPrivate(), JCSP.PROVIDER_NAME)).build(new GostContentSignerProvider(
                keyPair.getPrivate(), JCSP.PROVIDER_NAME), new JcaX509CertificateHolder(signerCert)));
        gen.addCertificates(certs);

        // Создаем совмещенную подпись PKCS7.
        CMSProcessableByteArray content = new CMSProcessableByteArray(data);
        CMSSignedData signedData = gen.generate(content, false);
//        CMSSignedData sigData = gen.generate(msg, false);
//        return sigData.getEncoded();
        // Сформированная подпись.
        byte[] pkcs7 = signedData.getEncoded();
        return pkcs7;
    }

    public static void saveByteArrayToFile(byte[] data, String filePath) {
        FileOutputStream fos = null;
        try {
            // Создаем FileOutputStream для указанного файла
            fos = new FileOutputStream(filePath);
            // Записываем массив байтов в файл
            fos.write(data);
            System.out.println("Файл успешно сохранен: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Закрываем поток, если он был открыт
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

