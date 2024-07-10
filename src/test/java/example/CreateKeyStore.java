package example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class CreateKeyStore {
    public static void main(String[] args) {
        try {
            // Créer un nouveau keystore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, null);

            // Enregistrer le keystore dans un fichier
            try (FileOutputStream fos = new FileOutputStream("store.jks")) {
                keyStore.store(fos, "changeit".toCharArray());
            }

            System.out.println("Keystore créé avec succès : store.jks");
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            e.printStackTrace();
        }
    }
}