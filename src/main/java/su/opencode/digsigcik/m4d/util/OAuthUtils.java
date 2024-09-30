package su.opencode.digsigcik.m4d.util;

import lombok.experimental.UtilityClass;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

@UtilityClass
public class OAuthUtils {
    private final Mac hmacSha256;

    static {
        try {
            hmacSha256 = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void init(byte[] key) {
        if (key.length != 32) {
            throw new IllegalArgumentException("Key must be 32 bytes long");
        }
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
        try {
            hmacSha256.init(secretKeySpec);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateState() {
        String state = UUID.randomUUID().toString();
        byte[] signature = hmacSha256.doFinal(state.getBytes());
        String signatureBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        return state + "." + signatureBase64;
    }
}

