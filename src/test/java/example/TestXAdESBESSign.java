package example;

import com.lowagie.text.DocumentException;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.CryptoPro.CAdES.exception.CAdESException;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCPxml.xmldsig.JCPXMLDSigInit;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.XAdES.DataObjects;
import ru.CryptoPro.XAdES.XAdESSignature;
import ru.CryptoPro.XAdES.XAdESType;
import ru.CryptoPro.XAdES.exception.XAdESException;
import ru.CryptoPro.XAdES.transform.EnvelopedTransform;
import ru.CryptoPro.reprov.RevCheck;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class TestXAdESBESSign {
    public static void main(String[] args) throws Exception {
           Security.addProvider(new JCP());
        //Security.addProvider(new JCSP());
        Security.addProvider(new RevCheck());
        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");
        //        System.setProperty("ocsp.enable", "true");
//        System.setProperty("com.sun.security.enableAIAcaIssuers", "true");
//        System.setProperty("ru.CryptoPro.reprov.enableAIAcaIssuers", "true");
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <Доверенность " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3" +
                ".org/2001/XMLSchema\" xmlns=\"urn://x-artefacts/EMCHD_1\" ВерсФорм=\"EMCHD_1\" ПрЭлФорм=\"00000000\"" +
                " ИдФайл=\"ON_EMCHD_20240718_270220da-575b-44a0-8490-8b91b9f0b8fe\" Id=\"acct\">     <Документ>         <Довер>  " +
                "           <СвДов ВидДовер=\"1\" ПрПередов=\"3\" ВнНомДовер=\"б/н\" " +
                "НомДовер=\"270220da-575b-44a0-8490-8b91b9f0b8fe\" ДатаВыдДовер=\"2024-06-19\" " +
                "СрокДейст=\"2025-06-18\">                 <СведСист>sep.cik</СведСист>             </СвДов>         " +
                "    <СвДоверит ТипДоверит=\"1\">                 <Доверит>                     <РосОргДовер " +
                "ЕИОУК=\"0\" ЕИОФЛ=\"1\" ЕИОИП=\"0\">                         <СвРосОрг НаимОрг=\"ОБЩЕСТВО С " +
                "ОГРАНИЧЕННОЙ ОТВЕТСТВЕННОСТЬЮ &quot;ФАБРИКА КАЧЕСТВА&quot;\" ИННЮЛ=\"7430030190\" КПП=\"743001001\" " +
                "ОГРН=\"1187456017682\">                             <АдрРег Регион=\"63\">                          " +
                "       <АдрРФ>456602, Челябинская область, г Копейск, ул Пилотов, д. 7а, помещ. 2</АдрРФ>           " +
                "                  </АдрРег>                         </СвРосОрг>                         <ЛицоБезДов " +
                "ПолнЮЛ=\"1\">                             <СвФЛ ИННФЛ=\"263516479611\" СНИЛС=\"126-029-036 24\" " +
                "Должность=\"Генеральный директор\">                                 <СведФЛ>                        " +
                "             <ФИО Фамилия=\"Петров\" Имя=\"Петр\" Отчество=\"Петрович\"/>                           " +
                "      </СведФЛ>                             </СвФЛ>                         </ЛицоБезДов>           " +
                "          </РосОргДовер>                 </Доверит>             </СвДоверит>             <СвУпПред " +
                "ТипПред=\"3\">                 <Пред>                     <СведФизЛ ИННФЛ=\"614207213728\" " +
                "СНИЛС=\"129-436-436 82\">                         <СведФЛ ДатаРожд=\"1971-09-16\">                  " +
                "           <ФИО Фамилия=\"Степанов\" Имя=\"Владимир\" Отчество=\"Иванович\"/>                       " +
                "      <УдЛичнФЛ КодВидДок=\"21\" СерНомДок=\"7318 145620\" ДатаДок=\"2019-07-27\" ВыдДок=\"УВД " +
                "РОССИИ ПО УЛЬЯНОВСКОЙ ОБЛАСТИ\" КодВыдДок=\"730-002\"/>                         </СведФЛ>           " +
                "          </СведФизЛ>                 </Пред>             </СвУпПред>             <СвПолн " +
                "ТипПолн=\"1\" ПрСовмПолн=\"1\" ПрУтрПолн=\"1\">                 <МашПолн " +
                "КодПолн=\"SKBKONTUR_EPGU_APIKEY\" НаимПолн=\"Формирование API Key для доступа к API Госуслуг" +
                ".Контур\"/>             </СвПолн>         </Довер>     </Документ> </Доверенность>";
        byte[] data = s.getBytes(StandardCharsets.UTF_8);
        createSignature(data);

        saveByteArrayToFile(data, "test.txt");
        System.out.println("Done");

    }

    public static void createSignature(byte[] data)
            throws Exception {
        String pass = "301214";
        KeyPair keyPair = null;
        X509Certificate certificate = null;
        Certificate[] certificateChain = new Certificate[0];
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
                        certificateChain = keyStore.getCertificateChain(alias);

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

     //   JCPXMLDSigInit.init();
        String ref_acct = "acct"; // ссылка на подписываемый узел
// декодирование документа
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        Document document = dbFactory.newDocumentBuilder()
                .parse(
                        new ByteArrayInputStream(data));
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile(String.format("//*[@Id='%s']", ref_acct));
        NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        Node node = nodes.item(0);
        String referenceURI = "#" + ref_acct;
// Подписываемая ссылка.
        DataObjects dataObjects = new DataObjects(Arrays.asList(referenceURI));
        dataObjects.addTransform(new EnvelopedTransform());
        XAdESSignature xAdESSignature = new XAdESSignature();
// добавляем подписанта формата XadES-BES. Также можно передать CRL для проверки цепочки
// подписанта вместо использования enableCRLDP
        //      Collections.reverse(certificates);
//        xAdESSignature.addSigner(JCP.PROVIDER_NAME, null, keyPair.getPrivate(), certificates, XAdESType.XAdES_BES,
//                null);
//        xAdESSignature.addSigner(JCSP.PROVIDER_NAME, JCP.GOST_DIGEST_OID,
//                JCP.GOST_EL_KEY_OID,
//                null, keyPair.getPrivate(), certificates, true,
//                XAdESType.XAdES_BES, tsaUrl, cRLs);
        xAdESSignature.addSigner(JCSP.PROVIDER_NAME,keyAlgToDigestOid(keyPair.getPrivate().getAlgorithm()),
                keyAlgToKeyAlgorithmOid(keyPair.getPrivate().getAlgorithm()),
                null, keyPair.getPrivate(), certificates, true,
                XAdESType.XAdES_BES, null, null);
        FileOutputStream fileOutputStream = new FileOutputStream("signed1.xml");
        xAdESSignature.open(fileOutputStream);
// Подписание.

        xAdESSignature.update((Element) node, dataObjects);
        xAdESSignature.close();

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


    public static boolean gostAlgoritm(String keyAlgName) {
        Set<String> gostAlgorithms = Set.of(
                JCP.GOST_EL_2012_256_NAME,
                JCP.GOST_DH_2012_256_NAME,
                JCP.GOST_PARAMS_SIG_2012_256_KEY_OID,
                JCP.GOST_PARAMS_EXC_2012_256_KEY_OID,
                JCP.GOST_EL_2012_512_NAME,
                JCP.GOST_DH_2012_512_NAME,
                JCP.GOST_PARAMS_SIG_2012_512_KEY_OID,
                JCP.GOST_PARAMS_EXC_2012_512_KEY_OID
        );
        return gostAlgorithms.contains(keyAlgName.toUpperCase());
    }

    public static String keyAlgToDigestOid(String keyAlgName) {
        Map<String, String> digestOidMap = Map.of(
                JCP.GOST_EL_2012_256_NAME, JCP.GOST_DIGEST_2012_256_OID,
                JCP.GOST_DH_2012_256_NAME, JCP.GOST_DIGEST_2012_256_OID,
                JCP.GOST_PARAMS_SIG_2012_256_KEY_OID, JCP.GOST_DIGEST_2012_256_OID,
                JCP.GOST_PARAMS_EXC_2012_256_KEY_OID, JCP.GOST_DIGEST_2012_256_OID,
                JCP.GOST_EL_2012_512_NAME, JCP.GOST_DIGEST_2012_512_OID,
                JCP.GOST_DH_2012_512_NAME, JCP.GOST_DIGEST_2012_512_OID,
                JCP.GOST_PARAMS_SIG_2012_512_KEY_OID, JCP.GOST_DIGEST_2012_512_OID,
                JCP.GOST_PARAMS_EXC_2012_512_KEY_OID, JCP.GOST_DIGEST_2012_512_OID
        );

        return digestOidMap.getOrDefault(keyAlgName.toUpperCase(), JCP.GOST_DIGEST_OID);
    }

    public static String keyAlgToKeyAlgorithmOid(String keyAlgName) {
        Map<String, String> keyAlgOidMap = Map.of(
                JCP.GOST_EL_2012_256_NAME, JCP.GOST_PARAMS_SIG_2012_256_KEY_OID,
                JCP.GOST_DH_2012_256_NAME, JCP.GOST_PARAMS_SIG_2012_256_KEY_OID,
                JCP.GOST_EL_2012_512_NAME, JCP.GOST_PARAMS_SIG_2012_512_KEY_OID,
                JCP.GOST_DH_2012_512_NAME, JCP.GOST_PARAMS_SIG_2012_512_KEY_OID
        );

        return keyAlgOidMap.getOrDefault(keyAlgName.toUpperCase(), JCP.GOST_EL_KEY_OID);
    }

}
