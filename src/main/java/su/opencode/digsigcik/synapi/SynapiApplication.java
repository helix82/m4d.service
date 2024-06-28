package su.opencode.digsigcik.synapi;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.CryptoPro.JCP.JCP;

import java.security.Security;

@SpringBootApplication
public class SynapiApplication {
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new JCP());
        SpringApplication.run(SynapiApplication.class, args);
    }
}
