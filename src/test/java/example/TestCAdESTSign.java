package example;

import com.lowagie.text.DocumentException;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.CAdESType;
import ru.CryptoPro.CAdES.exception.CAdESException;
import ru.CryptoPro.CAdES.tools.CAdESUtility;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.reprov.RevCheck;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

@Log4j2
public class TestCAdESTSign {
    public static void main(String[] args) throws IOException, DocumentException, CertificateEncodingException, OperatorCreationException, CMSException, CAdESException {
     //   Security.addProvider(new JCP());
        Security.addProvider(new JCSP());
        Security.addProvider(new RevCheck());
        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");
//        System.setProperty("com.sun.security.enableAIAcaIssuers", "true");
//        System.setProperty("ru.CryptoPro.reprov.enableAIAcaIssuers", "true");
        byte[] data = "qwerty".getBytes(StandardCharsets.UTF_8);
        byte [] s = createSignature(data);
        saveByteArrayToFile(s,"test.p7s");
        saveByteArrayToFile(data,"test.txt");
        System.out.println("Done");
    }
    public static byte[] createSignature(byte[] data)
            throws IOException, CAdESException, CMSException, CertificateEncodingException, OperatorCreationException {
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
                            certificates.add((X509Certificate)certificateChain[i]);
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

//        String filePath = "koren.crt"; // Путь к файлу PEM
//        X509Certificate rootCertificate = null;
//        try (FileInputStream inputStream = new FileInputStream(filePath)) {
//            // Создаем CertificateFactory для X.509 сертификатов
//            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
//
//            // Генерируем сертификат из входного потока
//            rootCertificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
//
//            System.out.println("Сертификат успешно загружен:");
//            System.out.println(rootCertificate);
//        } catch (IOException | CertificateException e) {
//            e.printStackTrace();
//        }
//        certificates.add(rootCertificate);
        //     CAdESUtility.initJCPAlgorithms();
//        CMSProcessableByteArray msg = new CMSProcessableByteArray(data);
//        JcaCertStore certs = new JcaCertStore(certificates);
//        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
//
//        gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
//                new GostDigestCalculatorProvider(keyPair.getPrivate(), JCSP.PROVIDER_NAME)).build(new GostContentSignerProvider(
//                keyPair.getPrivate(), JCSP.PROVIDER_NAME), certificate));
//        // Добавление дополнительных атрибутов для CA (если необходимо)
//        gen.addCertificates(certs);
//        CMSSignedData sigData = gen.generate(msg, false);
//        return sigData.getEncoded();
        JcaCertStore certs = new JcaCertStore(certificates);
        // Создаем CAdES подпись.
        CAdESUtility.initJCPAlgorithms();
        CAdESSignature cadesSignature = new CAdESSignature(true);
// Добавляем CAdES-BES подпись №1. Также можно передать CRL для проверки цепочки подписанта
// вместо использования enableCRLDP
//        cadesSignature.addSigner(JCSP.PROVIDER_NAME, JCP.GOST_DIGEST_OID,
//                JCP.GOST_EL_KEY_OID, keyPair.getPrivate(), certificates, CAdESType.CAdES_BES, null, false);
// Добавляем CAdES-X Long Type 1 подпись №2.
        cadesSignature.addSigner(JCSP.PROVIDER_NAME, JCP.GOST_DIGEST_OID,
                JCP.GOST_EL_KEY_OID, keyPair.getPrivate(), new ArrayList<>(Arrays.asList(certificate)), CAdESType.CAdES_T,
                "http://keylab/tsp/tsp.srf", false);
        ByteArrayOutputStream signatureStream = new ByteArrayOutputStream();
        cadesSignature.open(signatureStream); // подготовка контекста
        cadesSignature.update(data); // вычисление хэш-кода
        cadesSignature.setCertificateStore(certs);
        cadesSignature.close(); // создание подписи с выводом в signatureStream
        signatureStream.close();
// Получаем подпись с двумя подписантами в виде м
        byte[] cadesCms = signatureStream.toByteArray();
        return cadesCms;
    }

    public static String keyAlgToDigestOid(String keyAlgName) {
        if (keyAlgName.equalsIgnoreCase(JCP.GOST_EL_2012_256_NAME) ||
                keyAlgName.equalsIgnoreCase(JCP.GOST_DH_2012_256_NAME) ||
                keyAlgName.equalsIgnoreCase(JCP.GOST_PARAMS_SIG_2012_256_KEY_OID) ||
                keyAlgName.equalsIgnoreCase(JCP.GOST_PARAMS_EXC_2012_256_KEY_OID)) {
            return JCP.GOST_DIGEST_2012_256_OID;
        }
        else if (keyAlgName.equalsIgnoreCase(JCP.GOST_EL_2012_512_NAME) ||
                keyAlgName.equalsIgnoreCase(JCP.GOST_DH_2012_512_NAME) ||
                keyAlgName.equalsIgnoreCase(JCP.GOST_PARAMS_SIG_2012_512_KEY_OID) ||
                keyAlgName.equalsIgnoreCase(JCP.GOST_PARAMS_EXC_2012_512_KEY_OID)) {
            return JCP.GOST_DIGEST_2012_512_OID;
        }
        return JCP.GOST_DIGEST_OID;
    }
    public static String keyAlgToKeyAlgorithmOid(String keyAlgName) {
        if (keyAlgName.equalsIgnoreCase(JCP.GOST_EL_2012_256_NAME) ||
                (keyAlgName.equalsIgnoreCase(JCP.GOST_DH_2012_256_NAME))) {
            return JCP.GOST_PARAMS_SIG_2012_256_KEY_OID;
        }
        if (keyAlgName.equalsIgnoreCase(JCP.GOST_EL_2012_512_NAME) ||
                (keyAlgName.equalsIgnoreCase(JCP.GOST_DH_2012_512_NAME))) {
            return JCP.GOST_PARAMS_SIG_2012_512_KEY_OID;
        }
        return JCP.GOST_EL_KEY_OID;
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
