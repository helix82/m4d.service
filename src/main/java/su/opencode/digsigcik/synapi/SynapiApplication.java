package su.opencode.digsigcik.synapi;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.reprov.RevCheck;
import ru.CryptoPro.ssl.Provider;

import java.security.Security;

@SpringBootApplication
public class SynapiApplication {
    public static void main(String[] args) {
        try {
            System.setProperty("com.sun.security.enableCRLDP", "true");
            System.setProperty("com.ibm.security.enableCRLDP", "true");
            Security.addProvider(new BouncyCastleProvider());
            Security.addProvider(new JCSP()); // провайдер JCSP
            Security.addProvider(new JCP()); // провайдер JCP
            //  Security.addProvider(new JS()); // провайдер JCP
            Security.addProvider(new RevCheck());
            Security.addProvider(new Provider());
        } catch (Exception e) {
            System.err.println("Exception in initializing crypto pro : "+ e);
        } catch (Error er) {
            System.err.println("Error in initializing crypto pro : "+ er);
        }
        SpringApplication.run(SynapiApplication.class, args);
    }
}
