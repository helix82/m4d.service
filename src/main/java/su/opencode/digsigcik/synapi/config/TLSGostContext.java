package su.opencode.digsigcik.synapi.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import su.opencode.digsigcik.synapi.exception.TLSGostContextException;

public class TLSGostContext {

    private static final String PRIVATE_KEY_NOT_FOUND = "privateKey not found! ";
    private static final String TLS_GOST_CONTEXT_EXCEPTION = "TLSGostContextException: ";
    private static final Logger LOGGER = Logger.getLogger(TLSGostContext.class.getName());
    private static final char[] PASSWORD = "123456".toCharArray();
    private static final String SIGNER_KEY_ALIAS = "oper_ra";

    public SSLContext getContext() throws TLSGostContextException {

        SSLContext context = null;
        try {
            System.setProperty("ru.CryptoPro.reprov.enableCRLDP", "true");
            System.setProperty("com.sun.security.enableCRLDP", "true");

            System.setProperty("ssl.KeyManagerFactory.algorithm", "GostX509");
            System.setProperty("javax.net.ssl.keyStoreType", "HDIMAGE");
            System.setProperty("javax.net.ssl.keyStorePassword", "123456");
            System.setProperty("javax.net.ssl.trustStorePassword", "123456");

            KeyStore trustStore = KeyStore.getInstance("JKS");
            //trustStore.load((FileInputStream) ExampleService.class.getClassLoader().getResource("data/trustStore.jks").openStream(), PASSWORD);
            String trustStoreFileName = System.getProperty("javax.net.ssl.trustStore");
            trustStore.load(trustStoreFileName == null ? null : new FileInputStream(trustStoreFileName), System.getProperty("javax.net.ssl.trustStorePassword").toCharArray());

            KeyStore keyStore = KeyStore.getInstance("HDIMAGE", "JCSP");
            keyStore.load(null, PASSWORD);

        //    X509Certificate cert = (X509Certificate) keyStore.getCertificate(SIGNER_KEY_ALIAS);
        //    LOGGER.info("Signer found: " + ((X509Certificate) cert).getSubjectX500Principal().getName());

//            PrivateKey privateKey = null;
//            Key key = keyStore.getKey(SIGNER_KEY_ALIAS, PASSWORD);
//            if (key != null) {
//                privateKey = (PrivateKey) key;
//            } else {
//                LOGGER.log(Level.WARNING, PRIVATE_KEY_NOT_FOUND + SIGNER_KEY_ALIAS);
//                throw new TLSGostContextException(PRIVATE_KEY_NOT_FOUND + SIGNER_KEY_ALIAS);
//            }

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("GostX509", "JTLS");
            kmf.init(keyStore, PASSWORD);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("GostX509", "JTLS");
            tmf.init(trustStore);

            context = SSLContext.getInstance("GostTLS", "JTLS");
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        } catch (KeyStoreException | NoSuchProviderException e) {
            throw new TLSGostContextException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new TLSGostContextException(e);
        } catch (CertificateException e) {
            throw new TLSGostContextException(e);
        } catch (IOException e) {
            throw new TLSGostContextException(e);
        } catch (UnrecoverableKeyException e) {
            throw new TLSGostContextException(e);
        } catch (KeyManagementException e) {
            throw new TLSGostContextException(e);
        } catch (Exception e) {
            throw new TLSGostContextException(e);
        }
        return context;
    }

}