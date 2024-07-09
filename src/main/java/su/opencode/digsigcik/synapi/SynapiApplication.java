package su.opencode.digsigcik.synapi;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.CryptoPro.Crypto.CryptoProvider;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.reprov.RevCheck;
import ru.CryptoPro.ssl.Provider;


import java.security.Security;

@SpringBootApplication
public class SynapiApplication {
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        // CRL для проверки серверной цепочки будут
        // загружены из CRL DP.

//        System.setProperty("com.sun.security.enableCRLDP", "true");
//        System.setProperty("com.ibm.security.enableCRLDP", "true");

        Security.addProvider(new JCP());
        Security.addProvider(new CryptoProvider());
        Security.addProvider(new RevCheck());
        Security.addProvider(new Provider());
        SpringApplication.run(SynapiApplication.class, args);
    }
}
