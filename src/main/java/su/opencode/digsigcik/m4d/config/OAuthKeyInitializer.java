package su.opencode.digsigcik.m4d.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import su.opencode.digsigcik.m4d.util.OAuthUtils;

import javax.annotation.PostConstruct;
import java.util.Base64;

@Component
public class OAuthKeyInitializer {

    @Value("${oauth.hmac.key}")
    private String hmacKey;

    @PostConstruct
    public void init() {
        byte[] key = Base64.getDecoder().decode(hmacKey);

        if (key.length != 32) {
            throw new IllegalArgumentException("Ключ для State должен быть 32 байта длиной");
        }

        OAuthUtils.init(key);
    }
}

