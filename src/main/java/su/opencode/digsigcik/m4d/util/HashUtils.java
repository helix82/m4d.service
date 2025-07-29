package su.opencode.digsigcik.m4d.util;

import lombok.experimental.UtilityClass;
import org.bitcoinj.core.Base58;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

@UtilityClass
public class HashUtils {

    public static String getHash(byte[] origin, String salt) throws NoSuchAlgorithmException {
        String base64Encoded = Base64.toBase64String(origin);
        String saltedBase64 = salt + base64Encoded + salt;
        byte[] hash = hashGOST3411_2012_256(saltedBase64.getBytes(StandardCharsets.UTF_8));
        return Base58.encode(hash);
    }

    private static byte[] hashGOST3411_2012_256(byte[] input) throws NoSuchAlgorithmException {
        Security.addProvider(new BouncyCastleProvider());
        MessageDigest digest = MessageDigest.getInstance("GOST3411-2012-256");
        return digest.digest(input);
    }

}
