package su.opencode.digsigcik.m4d;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class M4dApplication {
    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.trustStore", System.getenv("JAVA_HOME")+"/lib/security/cacerts");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        SpringApplication.run(M4dApplication.class, args);
    }
}
